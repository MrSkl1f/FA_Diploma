package network

import database.DatabaseFactory
import kotlin.math.sqrt

class DataNormalizer {
    private val minSum = mutableListOf<Double>()
    private val maxSum = mutableListOf<Double>()
    private var learningSelection = mutableListOf<MutableList<Double>>()

    // Возвращает обучающую выборку с нормализацией
    fun getLearningSelection(indexes: List<Int>? = null): MutableList<MutableList<Double>> {
        DatabaseFactory.init()
        if (indexes == null) {
            readUsers()
        } else {
            readUsersWithIndexes(indexes)
        }

        addNewParameter()
        getSummaryForParameters()
        convertToCube()
        scaleSelection()
        return learningSelection
    }

    private fun readUsers() {
        try {
            learningSelection.addAll(database.readUsers())
        } catch (e: Exception) {
            println("Ошибка при чтении: ${e.message}")
        }
    }

    private fun readUsersWithIndexes(indexes: List<Int>) {
        try {
            learningSelection.addAll(database.readUsersWithIndexes(indexes))
        } catch (e: Exception) {
            println("Ошибка при чтении: ${e.message}")
        }
    }

    // Вычисляет минимальные и максимальные значения для каждого параметра
    private fun getSummaryForParameters() {
        val paramCount = learningSelection[0].size
        for (i in 0 until paramCount) {
            val elements = learningSelection.map { it[i] }
            maxSum.add(elements.maxOrNull() ?: 0.0)
            minSum.add(elements.minOrNull() ?: 0.0)
        }
    }

    // Добавляет новый параметр (длину вектора) к каждому элементу выборки
    private fun addNewParameter() {
        for (elements in learningSelection) {
            elements.add(sqrt(getLength(elements)))
        }
    }

    // Преобразует выборку в "куб" с диапазоном значений от -1 до 1
    private fun convertToCube() {
        for (elements in learningSelection) {
            for (i in elements.indices) {
                elements[i] = normalizeValue(elements[i], maxSum[i], minSum[i])
            }
        }
    }

    // Масштабирует выборку, чтобы все вектора находились на "сфере"
    private fun scaleSelection() {
        for (elements in learningSelection) {
            val length = sqrt(getLength(elements))
            for (i in elements.indices) {
                elements[i] /= length
            }
        }
    }

    // Вычисляет длину вектора
    private fun getLength(arr: List<Double>): Double {
        return arr.sumOf { it * it }
    }

    // Масштабирует значение x из диапазона [xMin, xMax] в диапазон [-1, 1]
    private fun normalizeValue(x: Double, xMax: Double, xMin: Double): Double {
        if (xMax == xMin) return 0.0 // предотвращаем деление на 0
        val div = xMax - xMin
        return -1.0 + ((x - xMin) / div) * 2.0
    }
}

fun main() {
    DataNormalizer().getLearningSelection().forEach { result -> println(result) }
}