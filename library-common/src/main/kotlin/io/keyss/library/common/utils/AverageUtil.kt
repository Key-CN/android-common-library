package io.keyss.library.common.utils

/**
 * @author Key
 * Time: 2022/02/16 16:55
 * Description:
 */
class AverageUtil {
    private val mDefaultValue: Double = 0.0
    private val mDataSet = mutableListOf<Double>()

    fun getAvg(): Double {
        return if (mDataSet.isEmpty()) {
            mDefaultValue
        } else {
            mDataSet.sum() / mDataSet.size
        }
    }

    fun getMin(): Double {
        return mDataSet.minOrNull() ?: mDefaultValue
    }

    fun getMax(): Double {
        return mDataSet.maxOrNull() ?: mDefaultValue
    }

    fun add(number: Number) {
        mDataSet.add(number.toDouble())
    }

    override fun toString(): String {
        return "min=${getMin()}, max=${getMax()}, avg=${getAvg()}, count=${mDataSet.size}"
    }
}