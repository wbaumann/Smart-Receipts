package co.smartreceipts.android.model.factory

import co.smartreceipts.android.DefaultObjects
import co.smartreceipts.android.model.impl.MultiplePriceImplNew
import co.smartreceipts.android.model.impl.SinglePriceImplNew
import co.smartreceipts.android.utils.TestLocaleToggler
import org.joda.money.CurrencyUnit
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal
import java.util.*

@RunWith(RobolectricTestRunner::class)
class PriceBuilderFactoryNewTest {

    companion object {
        val CURRENCY1: CurrencyUnit = CurrencyUnit.USD
        val CURRENCY2: CurrencyUnit = CurrencyUnit.EUR

        val PRICE1: BigDecimal = BigDecimal.ONE
        val PRICE2: BigDecimal = BigDecimal.TEN

        val EXCHANGE_RATE1 = ExchangeRateBuilderFactory().setBaseCurrency(CURRENCY1).build()
        val EXCHANGE_RATE2 = ExchangeRateBuilderFactory().setBaseCurrency(CURRENCY2).build()

        val SINGLE_PRICE1 = SinglePriceImplNew(PRICE1, CURRENCY1, EXCHANGE_RATE1)
        val SINGLE_PRICE2 = SinglePriceImplNew(PRICE2, CURRENCY2, EXCHANGE_RATE2)

        val MULTIPLE_PRICE1 = MultiplePriceImplNew(CURRENCY1, listOf(SINGLE_PRICE1, SINGLE_PRICE2))

        val PRICEABLE1 = DefaultObjects.newDefaultTrip(SINGLE_PRICE1)
        val PRICEABLE2 = DefaultObjects.newDefaultTrip(SINGLE_PRICE2)

    }

    @Before
    fun setUp() {
        TestLocaleToggler.setDefaultLocale(Locale.US)
    }

    @After
    fun tearDown() {
        TestLocaleToggler.resetDefaultLocale()
    }

    @Test
    fun singlePriceDefault() {
        val price = PriceBuilderFactoryNew().build()

        val expected =
            SinglePriceImplNew(BigDecimal.ZERO, CurrencyUnit.USD, ExchangeRateBuilderFactory().setBaseCurrency(CurrencyUnit.USD).build())

        assertEquals(expected, price)
    }

    @Test
    fun singlePriceBasedOnSinglePrice() {

        val singlePrice = SinglePriceImplNew(PRICE1, CURRENCY1, EXCHANGE_RATE1)

        val price = PriceBuilderFactoryNew(singlePrice)
            .build()

        assertEquals(singlePrice, price)
    }

    @Test
    fun singlePriceBasedOnSinglePriceRedefined() {

        val singlePrice = SinglePriceImplNew(PRICE1, CURRENCY1, EXCHANGE_RATE1)

        val price = PriceBuilderFactoryNew(singlePrice)
            .setCurrency(CURRENCY2)
            .build()

        val expected = SinglePriceImplNew(PRICE1, CURRENCY2, EXCHANGE_RATE1)

        assertEquals(expected, price)

    }

    @Test
    fun setPriceOverridePrices() {

        val price = PriceBuilderFactoryNew()
            .setPrices(listOf(SINGLE_PRICE1, SINGLE_PRICE2), CURRENCY1)
            .setPrice(SINGLE_PRICE1)
            .build()

        assertNotEquals(MULTIPLE_PRICE1, price)
        assertEquals(SINGLE_PRICE1, price)

    }

    @Test
    fun setPricesOverridePrice() {

        val price = PriceBuilderFactoryNew()
            .setPrice(SINGLE_PRICE1)
            .setPrices(listOf(SINGLE_PRICE1, SINGLE_PRICE2), CURRENCY1)
            .build()

        assertNotEquals(SINGLE_PRICE1, price)
        assertEquals(MULTIPLE_PRICE1, price)

    }

    @Test
    fun singlePriceBasedOnMultiplePrice() {

        val price = PriceBuilderFactoryNew(MULTIPLE_PRICE1)
            .build()

        assertEquals(SinglePriceImplNew(PRICE1.plus(PRICE2), CURRENCY1, EXCHANGE_RATE1), price)
    }

    @Test
    fun singlePriceBasedOnMultiplePriceRedefined() {

        val price = PriceBuilderFactoryNew(MULTIPLE_PRICE1)
            .setCurrency(CURRENCY2)
            .build()

        val expected = SinglePriceImplNew(PRICE1.plus(PRICE2), CURRENCY2, EXCHANGE_RATE2)

        assertEquals(expected, price)
    }

    @Test
    fun singlePriceWithoutExchangeRate() {
        val price = PriceBuilderFactoryNew()
            .setPrice(PRICE1)
            .setCurrency(CURRENCY1)
            .build()

        val expected = SinglePriceImplNew(PRICE1, CURRENCY1, EXCHANGE_RATE1)

        assertEquals(expected, price)
    }

    @Test
    fun singlePriceWithExchangeRate() {
        val price = PriceBuilderFactoryNew()
            .setPrice(PRICE1)
            .setCurrency(CURRENCY1)
            .setExchangeRate(EXCHANGE_RATE1)
            .build()

        val expected = SinglePriceImplNew(PRICE1, CURRENCY1, EXCHANGE_RATE1)

        assertEquals(expected, price)
    }

    @Test
    fun multiplePriceFromSinglePrices() {

        val price = PriceBuilderFactoryNew()
            .setPrices(listOf(SINGLE_PRICE1, SINGLE_PRICE2), CURRENCY1)
            .build()

        assertEquals(MULTIPLE_PRICE1, price)
    }

    @Test
    fun multiplePriceFromSinglePriceables() {
        val price = PriceBuilderFactoryNew()
            .setPriceables(listOf(PRICEABLE1, PRICEABLE2), CURRENCY1)
            .build()

        assertEquals(MULTIPLE_PRICE1, price)
    }

    @Test
    fun pricesAndPriceablesAreSame() {
        val price1 = PriceBuilderFactoryNew()
            .setPrices(listOf(SINGLE_PRICE1, SINGLE_PRICE2), CURRENCY1)
            .build()

        val price2 = PriceBuilderFactoryNew()
            .setPriceables(listOf(PRICEABLE1, PRICEABLE2), CURRENCY1)
            .build()

        assertEquals(price1, price2)
    }

    @Test
    fun multiplePriceFromMixedPrices() {

        val price = PriceBuilderFactoryNew()
            .setPrices(listOf(SINGLE_PRICE1, MULTIPLE_PRICE1), CURRENCY1)
            .build()

        val expected = MultiplePriceImplNew(CURRENCY1, listOf(SINGLE_PRICE1, MULTIPLE_PRICE1))

        assertEquals(expected, price)
    }
}