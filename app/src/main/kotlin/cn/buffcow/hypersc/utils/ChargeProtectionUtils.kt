package cn.buffcow.hypersc.utils

import android.content.Context
import android.provider.Settings
import com.highcapable.yukihookapi.hook.log.YLog
import miui.util.IMiCharge

/**
 * @author qingyu
 * <p>Create on 2025/01/03 17:12</p>
 */
object ChargeProtectionUtils {

    const val MIN_CHARGE_PERCENT_VALUE = 20
    const val MAX_CHARGE_PERCENT_VALUE = 100

    private const val KEY_SMART_CHARGE_PERCENT_VALUE = "smart_charge_percent_value"

    fun closeSmartCharge(): Boolean = setSmartChargeValue("0x10")

    fun openCommonProtectMode(value: Int): Boolean {
        val valueToSet = smartChargeValueToSet(value)
        val res = setSmartChargeValue(valueToSet)
        YLog.debug("openCommonProtectMode:$res, setValue:$valueToSet")
        return res
    }

    private fun getSmartChargeValue(): String? = try {
        IMiCharge.getInstance().getMiChargePath("smart_chg").also {
            YLog.debug("getSmartChargeValue res:$it")
        }
    } catch (th: Throwable) {
        YLog.error("getSmartChargeValue error:", th)
        null
    }

    private fun setSmartChargeValue(value: String): Boolean = try {
        IMiCharge.getInstance().setMiChargePath("smart_chg", value).also {
            YLog.debug("setSmartChargeValue:$value, res:$it")
        }
    } catch (th: Throwable) {
        YLog.error("setSmartChargeValue error:", th)
        false
    }

    fun getSmartChargePercentValue(ctx: Context): Int? {
        val value = ctx.getPercentValue() ?: return null
        return when {
            !isSmartChargePercentValueValid(value) -> {
                YLog.warn("smart charge percent value invalid, remove now")
                ctx.putPercentValue(null)
            }

            ensureSmartChargePercentValue(ctx, value, "read percent value") -> value

            else -> value
        }
    }

    fun ensureSmartChargePercentValue(ctx: Context, reason: String): Boolean {
        val value = ctx.getPercentValue()?.takeIf(::isSmartChargePercentValueValid) ?: return false
        return ensureSmartChargePercentValue(ctx, value, reason)
    }

    private fun ensureSmartChargePercentValue(ctx: Context, value: Int, reason: String): Boolean {
        val current = getSmartChargeValue()
        if (isExpectedSmartChargeValue(current, value)) return false

        val res = openCommonProtectMode(value)
        YLog.warn("smart_chg changed by sys or reboot, retry set:$res, reason:$reason, current:$current, percent:$value")
        if (!res) ctx.putPercentValue(null)
        return res
    }

    fun putSmartChargePercentValue(ctx: Context, value: Int?) {
        ctx.putPercentValue(value?.takeIf(ChargeProtectionUtils::isSmartChargePercentValueValid))
    }

    fun isSmartChargePercentValueValid(perValue: Int): Boolean {
        return perValue in MIN_CHARGE_PERCENT_VALUE..MAX_CHARGE_PERCENT_VALUE
    }

    private fun smartChargeValueToSet(value: Int): String {
        return "0x${((value shl 16) or 17).toString(16)}"
    }

    private fun isExpectedSmartChargeValue(current: String?, value: Int): Boolean {
        val expected = (value shl 16) or 17
        return current?.trim()?.let {
            it.equals(smartChargeValueToSet(value), ignoreCase = true) ||
                    it.toIntOrNull() == expected ||
                    it.removePrefix("0x").removePrefix("0X").toIntOrNull(16) == expected
        } == true
    }

    private fun Context.getPercentValue(): Int? {
        return Settings.System.getString(contentResolver, KEY_SMART_CHARGE_PERCENT_VALUE)?.toIntOrNull()
    }

    private fun Context.putPercentValue(value: Int?): Int? {
        return if (Settings.System.putString(
                contentResolver,
                KEY_SMART_CHARGE_PERCENT_VALUE,
                value?.toString()
            )
        ) value else null
    }
}
