package co.smartreceipts.android.model.factory

import co.smartreceipts.android.model.PriceNew
import co.smartreceipts.android.model.PriceableNew
import co.smartreceipts.android.model.gson.ExchangeRate
import co.smartreceipts.android.model.impl.*
import co.smartreceipts.android.model.utils.ModelUtils
import org.joda.money.CurrencyUnit
import java.math.BigDecimal
import java.util.*

/**
 * A [PriceNew] [BuilderFactory]
 * implementation, which will be used to generate instances of [PriceNew] objects
 */
class PriceBuilderFactoryNew : BuilderFactory<PriceNew> {
    private var priceDecimal: BigDecimal = BigDecimal.ZERO
    private var currency: CurrencyUnit = CurrencyUnit.of(Locale.getDefault())
    private var exchangeRate: ExchangeRate? = null
    private var priceables: List<PriceableNew>? = null
    private var prices: List<PriceNew>? = null


    constructor() {
        currency = CurrencyUnit.of(Locale.getDefault())
        priceDecimal = BigDecimal.ZERO
    }

    constructor(price: PriceNew) {
        priceDecimal = price.price
        currency = price.currency
        exchangeRate = price.exchangeRate
    }

    fun setPrice(price: PriceNew): PriceBuilderFactoryNew {
        priceDecimal = price.price
        currency = price.currency
        exchangeRate = price.exchangeRate
        return this
    }

    fun setPrice(price: String): PriceBuilderFactoryNew {
        priceDecimal = ModelUtils.tryParse(price)
        return this
    }

    fun setPrice(price: Double): PriceBuilderFactoryNew {
        priceDecimal = BigDecimal.valueOf(price)
        return this
    }

    fun setPrice(price: BigDecimal): PriceBuilderFactoryNew {
        priceDecimal = price
        return this
    }

    fun setCurrency(currency: CurrencyUnit): PriceBuilderFactoryNew {
        this.currency = currency
        return this
    }

    fun setCurrency(currencyCode: String): PriceBuilderFactoryNew {
        currency = CurrencyUnit.of(currencyCode)
        return this
    }

    fun setExchangeRate(exchangeRate: ExchangeRate): PriceBuilderFactoryNew {
        this.exchangeRate = exchangeRate
        return this
    }

    fun setPrices(prices: List<PriceNew>, desiredCurrency: CurrencyUnit): PriceBuilderFactoryNew {
        this.prices = ArrayList(prices)
        currency = desiredCurrency
        return this
    }

    fun setPriceables(priceables: List<PriceableNew>, desiredCurrency: CurrencyUnit): PriceBuilderFactoryNew {
        this.priceables = ArrayList(priceables)
        currency = desiredCurrency
        return this
    }

    override fun build(): PriceNew {

        return when {
            !prices.isNullOrEmpty() -> MultiplePriceImplNew(currency, prices!!)

            !priceables.isNullOrEmpty() -> {
                val actualPrices = priceables!!.map { it.price }
                MultiplePriceImplNew(currency, actualPrices)
            }
            else -> {
                SinglePriceImplNew(priceDecimal, currency, exchangeRate ?: ExchangeRateBuilderFactory().setBaseCurrency(currency).build())
            }
        }
    }
}