package co.smartreceipts.android.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.imports.CameraInteractionController;
import co.smartreceipts.android.imports.RequestCodes;
import co.smartreceipts.android.imports.importer.ActivityFileResultImporter;
import co.smartreceipts.android.imports.locator.ActivityFileResultLocator;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.ocr.OcrManager;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController;
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType;
import co.smartreceipts.android.utils.IntentUtils;
import co.smartreceipts.android.utils.log.Logger;
import co.smartreceipts.android.widget.ui.PinchToZoomImageView;
import dagger.Lazy;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.disposables.CompositeDisposable;
import wb.android.flex.Flex;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class ReceiptImageFragment extends WBFragment {

    // TODO: 08.07.2019 make sure that we're not leaving garbage photos
    // TODO: 08.07.2019 recheck image quality
    // TODO: 11.07.2019 convert ReceiptImageFragment to Kotlin
    // TODO: 14.07.2019 if crop action is canceled - show dialog for disabling this feature
    // TODO: 14.07.2019 check tests
    // TODO: 14.07.2019 add setting to disable crop

    // Save state
    private static final String KEY_OUT_RECEIPT = "key_out_receipt";
    private static final String KEY_OUT_URI = "key_out_uri";

    @Inject
    Flex flex;

    @Inject
    PersistenceManager persistenceManager;

    @Inject
    Analytics analytics;

    @Inject
    ReceiptTableController receiptTableController;

    @Inject
    OcrManager ocrManager;

    @Inject
    NavigationHandler navigationHandler;

    @Inject
    ActivityFileResultLocator activityFileResultLocator;

    @Inject
    ActivityFileResultImporter activityFileResultImporter;

    @Inject
    Lazy<Picasso> picasso;

    private PinchToZoomImageView imageView;
    private ProgressBar progress;
    private TextView retakePhoto;
    private TextView editPhoto;
    private Toolbar toolbar;

    private Receipt receipt;
    private ImageUpdatedListener imageUpdatedListener;
    private CompositeDisposable compositeDisposable;
    private Uri imageUri;

    public static ReceiptImageFragment newInstance() {
        return new ReceiptImageFragment();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            receipt = getArguments().getParcelable(Receipt.PARCEL_KEY);
        } else {
            receipt = savedInstanceState.getParcelable(KEY_OUT_RECEIPT);
            imageUri = savedInstanceState.getParcelable(KEY_OUT_URI);
        }
        imageUpdatedListener = new ImageUpdatedListener();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.receipt_image_view, container, false);
        imageView = rootView.findViewById(R.id.receiptimagefragment_imageview);
        progress = rootView.findViewById(R.id.progress);
        retakePhoto = rootView.findViewById(R.id.button_retake_photo);
        editPhoto = rootView.findViewById(R.id.button_edit_photo);

        editPhoto.setOnClickListener(view -> {
            analytics.record(Events.Receipts.ReceiptImageViewEditPhoto);
            navigationHandler.navigateToCropActivity(this, Uri.fromFile(receipt.getFile()), RequestCodes.EDIT_IMAGE_CROP);
        });

        retakePhoto.setOnClickListener(view -> {
            analytics.record(Events.Receipts.ReceiptImageViewRetakePhoto);
            imageUri = new CameraInteractionController(ReceiptImageFragment.this).retakePhoto(receipt);
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadImage();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar = getActivity().findViewById(R.id.toolbar);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Logger.debug(this, "Result Code: " + resultCode);
        if (receipt == null) {
            receipt = getArguments().getParcelable(Receipt.PARCEL_KEY);
        }

        // Show the progress bar
        if (resultCode != RESULT_CANCELED) {
            progress.setVisibility(View.VISIBLE);
        }

        // Null out the last request
        final Uri cachedImageSaveLocation = imageUri;
        imageUri = null;

        if (requestCode == RequestCodes.EDIT_IMAGE_CROP) {
            if (resultCode == RESULT_OK) {
                picasso.get().invalidate(UCrop.getOutput(data));
                loadImage();
            } else if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(data);
                if (cropError != null) {
                    Logger.error(this, "An error occurred while cropping the image: {}", cropError);
                }
            }
        } else {
            activityFileResultLocator.onActivityResult(requestCode, resultCode, data, cachedImageSaveLocation);
        }
    }

    private void subscribe() {
        compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(activityFileResultLocator.getUriStream()
                // uri always has SCHEME_CONTENT -> we don't need to check permissions
                .subscribe(locatorResponse -> {
                    if (!locatorResponse.getThrowable().isPresent()) {
                        progress.setVisibility(View.VISIBLE);
                        activityFileResultImporter.importFile(locatorResponse.getRequestCode(),
                                locatorResponse.getResultCode(), locatorResponse.getUri(), receipt.getTrip());
                    } else {
                        Toast.makeText(getActivity(), getFlexString(R.string.FILE_SAVE_ERROR), Toast.LENGTH_SHORT).show();
                        progress.setVisibility(View.GONE);
                        activityFileResultLocator.markThatResultsWereConsumed();
                    }
                }));

        compositeDisposable.add(activityFileResultImporter.getResultStream()
                .subscribe(response -> {
                    if (!response.getThrowable().isPresent()) {
                        final Receipt retakeReceipt = new ReceiptBuilderFactory(receipt).setFile(response.getFile()).build();
                        receiptTableController.update(receipt, retakeReceipt, new DatabaseOperationMetadata());
                    } else {
                        Toast.makeText(getActivity(), getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show();
                    }
                    progress.setVisibility(View.GONE);
                    activityFileResultLocator.markThatResultsWereConsumed();
                    activityFileResultImporter.markThatResultsWereConsumed();
                }));
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(receipt.getName());
        }
        receiptTableController.subscribe(imageUpdatedListener);

        subscribe();

    }

    @Override
    public void onPause() {
        receiptTableController.unsubscribe(imageUpdatedListener);

        compositeDisposable.clear();

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.debug(this, "onSaveInstanceState");
        outState.putParcelable(KEY_OUT_RECEIPT, receipt);
        outState.putParcelable(KEY_OUT_URI, imageUri);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigationHandler.navigateBack();
                return true;
            case R.id.action_share:
                if (receipt.getFile() != null) {
                    final Intent sendIntent = IntentUtils.getSendIntent(getActivity(), receipt.getFile());
                    startActivity(Intent.createChooser(sendIntent, getResources().getString(R.string.send_email)));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadImage() {
        if (receipt.getFile() != null && receipt.hasImage()) {
            picasso.get().load(receipt.getFile()).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).fit().centerInside().into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                    progress.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);

                    editPhoto.setVisibility(View.VISIBLE);
                    retakePhoto.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(Exception e) {
                    progress.setVisibility(View.GONE);
                    if (getActivity() != null) {
                        Toast.makeText(getActivity(), getFlexString(R.string.IMG_OPEN_ERROR), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    private class ImageUpdatedListener extends StubTableEventsListener<Receipt> {

        @Override
        public void onUpdateSuccess(@NonNull Receipt oldReceipt, @NonNull Receipt newReceipt, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
            if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
                if (oldReceipt.equals(receipt)) {
                    receipt = newReceipt;
                    loadImage();
                }
            }
        }

        @Override
        public void onUpdateFailure(@NonNull Receipt oldReceipt, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
            if (databaseOperationMetadata.getOperationFamilyType() != OperationFamilyType.Sync) {
                progress.setVisibility(View.GONE);
                Toast.makeText(getActivity(), getFlexString(R.string.database_error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFlexString(int id) {
        return getFlexString(flex, id);
    }

}