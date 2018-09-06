package co.smartreceipts.android.model.impl;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import java.io.File;
import java.sql.Date;
import java.util.TimeZone;

import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.PaymentMethod;
import co.smartreceipts.android.model.Price;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Source;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.utils.ModelUtils;
import co.smartreceipts.android.persistence.DatabaseHelper;
import co.smartreceipts.android.sync.model.SyncState;

/**
 * A mostly immutable implementation of the {@link co.smartreceipts.android.model.Receipt} interface that
 * serves as the default implementation.
 */
public final class DefaultReceiptImpl implements Receipt {

    private final int id;
    private final Trip trip;
    private final PaymentMethod paymentMethod;
    private final int index; // Tracks the index in the list (if specified)
    private final String name;
    private final String comment;
    private final Category category;
    private final Price price, tax;
    private final Date date;
    private final TimeZone timeZone;
    private final boolean isReimbursable;
    private final boolean isFullPage;
    private final Source source;
    private final String extraEditText1;
    private final String extraEditText2;
    private final String extraEditText3;
    private final SyncState syncState;
    private boolean isSelected;
    private File file;
    private long fileLastModifiedTime;
    private final long customOrderId;

    public DefaultReceiptImpl(int id, int index, @NonNull Trip trip, @Nullable File file, @NonNull PaymentMethod paymentMethod, @NonNull String name,
                              @NonNull Category category, @NonNull String comment, @NonNull Price price, @NonNull Price tax, @NonNull Date date,
                              @NonNull TimeZone timeZone, boolean isReimbursable, boolean isFullPage, boolean isSelected,
                              @NonNull Source source, @Nullable String extraEditText1, @Nullable String extraEditText2, @Nullable String extraEditText3,
                              @NonNull SyncState syncState, long customOrderId) {

        this.trip = Preconditions.checkNotNull(trip);
        this.name = Preconditions.checkNotNull(name);
        this.category = Preconditions.checkNotNull(category);
        this.comment = Preconditions.checkNotNull(comment);
        this.source = Preconditions.checkNotNull(source);
        this.price = Preconditions.checkNotNull(price);
        this.tax = Preconditions.checkNotNull(tax);
        this.date = Preconditions.checkNotNull(date);
        this.timeZone = Preconditions.checkNotNull(timeZone);
        this.syncState = Preconditions.checkNotNull(syncState);
        this.paymentMethod = Preconditions.checkNotNull(paymentMethod);

        this.id = id;
        this.index = index;
        this.file = file;
        fileLastModifiedTime = file != null ? file.lastModified() : -1;
        this.isReimbursable = isReimbursable;
        this.isFullPage = isFullPage;
        this.extraEditText1 = extraEditText1;
        this.extraEditText2 = extraEditText2;
        this.extraEditText3 = extraEditText3;
        this.isSelected = isSelected;
        this.customOrderId = customOrderId;
    }

    private DefaultReceiptImpl(@NonNull Parcel in) {
        trip = in.readParcelable(Trip.class.getClassLoader());
        paymentMethod = in.readParcelable(PaymentMethod.class.getClassLoader());
        id = in.readInt();
        name = in.readString();
        category = in.readParcelable(Category.class.getClassLoader());
        comment = in.readString();
        price = in.readParcelable(Price.class.getClassLoader());
        tax = in.readParcelable(Price.class.getClassLoader());
        final String fileName = in.readString();
        file = TextUtils.isEmpty(fileName) ? null : new File(fileName);
        fileLastModifiedTime = in.readLong();
        date = new Date(in.readLong());
        isReimbursable = (in.readByte() != 0);
        isFullPage = (in.readByte() != 0);
        isSelected = (in.readByte() != 0);
        extraEditText1 = in.readString();
        extraEditText2 = in.readString();
        extraEditText3 = in.readString();
        index = in.readInt();
        timeZone = TimeZone.getTimeZone(in.readString());
        syncState = in.readParcelable(SyncState.class.getClassLoader());
        source = Source.Parcel;
        customOrderId = in.readLong();
    }

    @Override
    public int getId() {
        return id;
    }

    @NonNull
    @Override
    public Trip getTrip() {
        return trip;
    }

    @NonNull
    @Override
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasImage() {
        if (file != null) {
            return file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg") || file.getName().endsWith(".png");
        } else {
            return false;
        }
    }

    @Override
    public boolean hasPDF() {
        if (file != null) {
            return file.getName().endsWith(".pdf");
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public File getImage() {
        return file;
    }

    @Nullable
    @Override
    public File getPDF() {
        return file;
    }

    @Nullable
    @Override
    public File getFile() {
        return file;
    }

    @NonNull
    @Override
    public String getFilePath() {
        if (file != null) {
            return file.getAbsolutePath();
        } else {
            return "";
        }
    }

    @NonNull
    @Override
    public String getFileName() {
        if (file != null) {
            return file.getName();
        } else {
            return "";
        }
    }

    @Override
    public long getFileLastModifiedTime() {
        return fileLastModifiedTime;
    }

    @NonNull
    @Override
    public Source getSource() {
        return source;
    }

    @NonNull
    @Override
    public Category getCategory() {
        return category;
    }

    @NonNull
    @Override
    public String getComment() {
        return comment;
    }

    @NonNull
    @Override
    public Price getPrice() {
        return price;
    }

    @NonNull
    @Override
    public Price getTax() {
        return tax;
    }

    @NonNull
    @Override
    public Date getDate() {
        return date;
    }

    @NonNull
    @Override
    public String getFormattedDate(@NonNull Context context, @NonNull String separator) {
        return ModelUtils.getFormattedDate(date, (timeZone != null) ? timeZone : TimeZone.getDefault(), context, separator);
    }

    @NonNull
    @Override
    public TimeZone getTimeZone() {
        return (timeZone != null) ? timeZone : TimeZone.getDefault();
    }

    @Override
    public boolean isReimbursable() {
        return isReimbursable;
    }

    @Override
    public boolean isFullPage() {
        return isFullPage;
    }

    @Override
    public boolean isSelected() {
        return isSelected;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public long getCustomOrderId() {
        return customOrderId;
    }

    @Nullable
    @Override
    public String getExtraEditText1() {
        if (DatabaseHelper.NO_DATA.equals(extraEditText1)) {
            return null;
        } else {
            return extraEditText1;
        }
    }

    @Nullable
    @Override
    public String getExtraEditText2() {
        if (DatabaseHelper.NO_DATA.equals(extraEditText2)) {
            return null;
        } else {
            return extraEditText2;
        }
    }

    @Nullable
    @Override
    public String getExtraEditText3() {
        if (DatabaseHelper.NO_DATA.equals(extraEditText3)) {
            return null;
        } else {
            return extraEditText3;
        }
    }

    @Override
    public boolean hasExtraEditText1() {
        return (extraEditText1 != null) && !extraEditText1.equals(DatabaseHelper.NO_DATA);
    }

    @Override
    public boolean hasExtraEditText2() {
        return (extraEditText2 != null) && !extraEditText2.equals(DatabaseHelper.NO_DATA);
    }

    @Override
    public boolean hasExtraEditText3() {
        return (extraEditText3 != null) && !extraEditText3.equals(DatabaseHelper.NO_DATA);
    }

    @NonNull
    @Override
    public SyncState getSyncState() {
        return syncState;
    }

    @Override
    public String toString() {
        return "DefaultReceiptImpl{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", trip=" + trip.getName() +
                ", paymentMethod=" + paymentMethod +
                ", index=" + index +
                ", comment='" + comment + '\'' +
                ", category=" + category +
                ", price=" + price.getCurrencyFormattedPrice() +
                ", tax=" + tax +
                ", date=" + date +
                ", timeZone=" + timeZone.getID() +
                ", isReimbursable=" + isReimbursable +
                ", isFullPage=" + isFullPage +
                ", source=" + source +
                ", extraEditText1='" + extraEditText1 + '\'' +
                ", extraEditText2='" + extraEditText2 + '\'' +
                ", extraEditText3='" + extraEditText3 + '\'' +
                ", isSelected=" + isSelected +
                ", file=" + file +
                ", fileLastModifiedTime=" + fileLastModifiedTime +
                ", customOrderId=" + customOrderId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DefaultReceiptImpl)) return false;

        DefaultReceiptImpl that = (DefaultReceiptImpl) o;

        if (id != that.id) return false;
        if (isReimbursable != that.isReimbursable) return false;
        if (isFullPage != that.isFullPage) return false;
        if (!trip.equals(that.trip)) return false;
        if (!paymentMethod.equals(that.paymentMethod)) return false;
        if (index != that.index) return false;
        if (!name.equals(that.name)) return false;
        if (!comment.equals(that.comment)) return false;
        if (!category.equals(that.category)) return false;
        if (!price.equals(that.price)) return false;
        if (!tax.equals(that.tax)) return false;
        if (!date.equals(that.date)) return false;
        if (!timeZone.equals(that.timeZone)) return false;
        if (extraEditText1 != null ? !extraEditText1.equals(that.extraEditText1) : that.extraEditText1 != null)
            return false;
        if (extraEditText2 != null ? !extraEditText2.equals(that.extraEditText2) : that.extraEditText2 != null)
            return false;
        if (extraEditText3 != null ? !extraEditText3.equals(that.extraEditText3) : that.extraEditText3 != null)
            return false;
        if (fileLastModifiedTime != that.fileLastModifiedTime) return false;
        if (customOrderId != that.customOrderId) return false;
        return file != null ? file.equals(that.file) : that.file == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + trip.hashCode();
        result = 31 * result + paymentMethod.hashCode();
        result = 31 * result + index;
        result = 31 * result + name.hashCode();
        result = 31 * result + comment.hashCode();
        result = 31 * result + category.hashCode();
        result = 31 * result + price.hashCode();
        result = 31 * result + tax.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + timeZone.hashCode();
        result = 31 * result + (isReimbursable ? 1 : 0);
        result = 31 * result + (isFullPage ? 1 : 0);
        result = 31 * result + (extraEditText1 != null ? extraEditText1.hashCode() : 0);
        result = 31 * result + (extraEditText2 != null ? extraEditText2.hashCode() : 0);
        result = 31 * result + (extraEditText3 != null ? extraEditText3.hashCode() : 0);
        result = 31 * result + (file != null ? file.hashCode() : 0);
        result = 31 * result + (int) fileLastModifiedTime;
        result = 31 * result + (int) (customOrderId ^ (customOrderId >>> 32));
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(getTrip(), flags);
        dest.writeParcelable(getPaymentMethod(), flags);
        dest.writeInt(getId());
        dest.writeString(getName());
        dest.writeParcelable(getCategory(), flags);
        dest.writeString(getComment());
        dest.writeParcelable(getPrice(), flags);
        dest.writeParcelable(getTax(), flags);
        dest.writeString(getFilePath());
        dest.writeLong(getFileLastModifiedTime());
        dest.writeLong(getDate().getTime());
        dest.writeByte((byte) (isReimbursable() ? 1 : 0));
        dest.writeByte((byte) (isFullPage() ? 1 : 0));
        dest.writeByte((byte) (isSelected() ? 1 : 0));
        dest.writeString(getExtraEditText1());
        dest.writeString(getExtraEditText2());
        dest.writeString(getExtraEditText3());
        dest.writeInt(getIndex());
        dest.writeString(timeZone.getID());
        dest.writeParcelable(getSyncState(), flags);
        dest.writeLong(getCustomOrderId());
    }

    public static Creator<DefaultReceiptImpl> CREATOR = new Creator<DefaultReceiptImpl>() {

        @Override
        public DefaultReceiptImpl createFromParcel(Parcel source) {
            return new DefaultReceiptImpl(source);
        }

        @Override
        public DefaultReceiptImpl[] newArray(int size) {
            return new DefaultReceiptImpl[size];
        }

    };

    @Override
    public int compareTo(@NonNull Receipt receipt) {
        if (customOrderId == receipt.getCustomOrderId()) {
            return receipt.getDate().compareTo(date);
        } else {
            return -Long.compare(customOrderId, receipt.getCustomOrderId());
        }
    }
}