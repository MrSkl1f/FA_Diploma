package network

import kotlinx.io.files.FileNotFoundException
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.io.File
import javax.swing.JFrame
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class Network(private var n: Int = 50) {

	private var m: Int? = null  // Размерность входных векторов
	private var selection: MutableList<MutableList<Double>> = mutableListOf()  // Обучающая выборка
	var neurons: MutableList<Neuron> = ArrayList()  // Список нейронов

	/**
	 * Создаёт сеть и загружает обучающую выборку с нормализацией.
	 */
	fun createForLearn() {
		val normalizer = DataNormalizer()
		selection = normalizer.getLearningSelection()  // Загружаем нормализованные данные
		create()  // Инициализируем нейроны
	}

	/**
	 * Создаёт сеть и загружает обучающую выборку с заданным набором данных.
	 *
	 * @param selection - обучающая выборка
	 */
	fun createForLearnWithSelection(selection: MutableList<MutableList<Double>>) {
		this.selection = selection
		create()  // Инициализируем нейроны
	}

	/**
	 * Метод обучения сети.
	 * Обучает нейроны на выборке с учетом параметров скорости обучения, количества нейронов и коэффициента сжатия.
	 *
	 * @param nu - скорость обучения
	 * @param cNeurons - количество обучаемых нейронов за один шаг
	 * @param alpha - коэффициент сжатия
	 * @param index - индекс для изменения параметров (по умолчанию -1)
	 */
	fun learn(nuIn: Double = 1.0, cNeuronsIn: Int = 4, alphaIn: Double = 0.01, index: Int = -1) {
		var nu = nuIn
		val number = 10  // Количество эпох

		var cNeuronsDelta = 0.0
		var cNeurons = cNeuronsIn  // Исходное количество нейронов для обучения

		var alpha = alphaIn
		val alpha0 = 0.01  // Начальный коэффициент сжатия
		val nu0 = nu

		val M = sqrt(m!!.toDouble())  // Размерность нормализованного входного вектора

		var t = 0  // Время, число эпох
		var deltaT = 20  // Время для увеличения числа нейронов

		val k = 5  // Количество случайных векторов для каждого шага

		if (index == 12) {
			deltaT = 100  // Увеличиваем deltaT для теста
		}

		while (alpha < 1) {
			for (i in 1 until number) {
				var currentVector = selection[Random.nextInt(selection.size)]  // Случайный вектор из выборки
				for (j in selection.indices) {
					val vector = currentVector.map { it * alpha + (1 - alpha) / M }.toMutableList()  // Корректируем вектор
					neuronTraining(nu, vector, cNeurons)  // Обучаем нейрон
					currentVector = getRandomVector(currentVector, selection, k)  // Получаем новый вектор для обучения
				}
			}

			t += deltaT
			cNeuronsDelta += 0.2
			alpha = if (alpha < 1) alpha0 * sqrt(t + 1f) else 1.0  // Обновляем коэффициент сжатия
			cNeurons = if (cNeurons <= 1) 1 else (cNeurons / sqrt(cNeuronsDelta + 1)).toInt()  // Обновляем количество нейронов
			nu = nu0 / sqrt(t + 1f)  // Обновляем скорость обучения

			println("alpha $alpha")
		}
	}

	/**
	 * Инициализирует нейроны на основе размерности выборки.
	 */
	private fun create() {
		m = selection[0].size  // Размерность вектора
		neurons = ArrayList()
		repeat(n) { neurons.add(Neuron(m!!)) }  // Создаем нейроны с количеством входных параметров m
	}

	/**
	 * Находит индексы нейронов с минимальным расстоянием.
	 */
	private fun findMinIndex(distances: List<Pair<Double, Int>>, cNeurons: Int): List<List<Int>> {
		return distances.sortedBy { it.first }.take(cNeurons).map { listOf(it.second) }
	}

	/**
	 * Обучает нейроны с корректировкой весов.
	 */
	private fun neuronTraining(nu: Double, inputVector: MutableList<Double>, cNeurons: Int) {
		val minIndex = getNeuronWinner(inputVector, cNeurons)  // Находим победителей
		for (i in minIndex.indices) {
			minimizeDifference(nu, neurons[minIndex[i][0]], inputVector)  // Обновляем веса победивших нейронов
		}
	}

	/**
	 * Находит победивший нейрон по методу ближайшего соседа для входного вектора.
	 * Возвращает список индексов победивших нейронов.
	 *
	 * @param inputVector - входной вектор
	 * @param cNeurons - количество победивших нейронов
	 * @return Список индексов победивших нейронов
	 */
	private fun getNeuronWinner(inputVector: List<Double>, cNeurons: Int = 1): List<List<Int>> {
		val distances = mutableListOf<Pair<Double, Int>>()
		for (i in neurons.indices) {
			var summ = 0.0
			for (j in inputVector.indices) {
				summ += (inputVector[j] - neurons[i].w[j]) * (inputVector[j] - neurons[i].w[j])
			}
			distances.add(Pair(sqrt(summ), i))  // Рассчитываем евклидово расстояние
		}
		return findMinIndex(distances, cNeurons)  // Находим минимальные индексы (победителей)
	}


	/**
	 * Находит индекс вектора с максимальным расстоянием.
	 */
	private fun findMaxIndex(distances: MutableList<Double>): Int {
		var index = 0
		var maxElem = distances[0]
		for (i in 1 until distances.size) {
			if (distances[i] > maxElem) {
				index = i
				maxElem = distances[i]
			}
		}
		return index
	}

	/**
	 * Вычисляет расстояния между текущим вектором и всеми векторами обучающей выборки.
	 */
	private fun getDistances(current: MutableList<Double>, vectors: MutableList<MutableList<Double>>): MutableList<Double> {
		return vectors.map { vector ->
			var summ = 0.0
			for (i in current.indices) {
				summ += (current[i] - vector[i]) * (current[i] - vector[i])  // Евклидово расстояние
			}
			summ
		}
			.toMutableList()
	}

	/**
	 * Получает случайный вектор из обучающей выборки, который наиболее удален от текущего.
	 */
	private fun getRandomVector(current: MutableList<Double>, vectors: MutableList<MutableList<Double>>, k: Int): MutableList<Double> {
		val indexes = MutableList(k) { Random.nextInt(vectors.size) }  // Генерируем случайные индексы
		val neededVectors = indexes.map { vectors[it].toMutableList() }.toMutableList()  // Берем векторы по индексам
		val distances = getDistances(current, neededVectors)  // Вычисляем расстояния
		val neededIndex = findMaxIndex(distances)  // Находим вектор с максимальным расстоянием
		return vectors[indexes[neededIndex]].toMutableList()  // Возвращаем этот вектор
	}

	/**
	 * Корректирует веса нейрона, уменьшая разницу между текущими весами и входным вектором.
	 */
	private fun minimizeDifference(nu: Double, neuron: Neuron, inputVector: MutableList<Double>) {
		for (index in neuron.w.indices) {
			neuron.w[index] += nu * (inputVector[index] - neuron.w[index])  // Обновляем веса
		}
	}

	fun setSelection(selection: MutableList<MutableList<Double>>) {
		this.selection = selection
	}

	fun saveNetwork(file: String = "./Dataset/neurons.csv") {
		val fileDir = File(file).parentFile
		if (!fileDir.exists()) {
			fileDir.mkdirs()  // Создаст каталог, если его нет
		}

		File(file).bufferedWriter().use { writer ->
			for (neurons in this.neurons) {
				writer.write(neurons.w.joinToString(separator = ";") + "\n")
			}
		}
	}

	fun loadNetwork(file: String = "./Dataset/neurons.csv") {
		// Проверяем, существует ли файл
		val fileObj = File(file)
		if (!fileObj.exists()) {
			throw FileNotFoundException("File not found: $file")
		}

		// Считываем данные из файла
		fileObj.bufferedReader().use { reader ->
			reader.forEachLine { line ->
				val weights = line.split(";").map { it.toDouble() } // Преобразуем строку в список весов
				neurons.add(Neuron(weights)) // Создаем объект Neuron и добавляем его в список
			}
		}
		n = neurons.size
	}

	fun calculateVariance() {
		for (neuron in neurons) {
			neuron.variance = neuron.vectors.map { it.toDoubleArray() }
				.let { vectors ->
					val dimension = vectors.firstOrNull()?.size ?: 0
					if (dimension == 0) return@let 0.0

					// Вычисляем среднее значение для каждой размерности
					val means = DoubleArray(dimension) { dim ->
						vectors.sumOf { it[dim] } / vectors.size
					}

					// Вычисляем дисперсию по всем векторам и размерностям
					vectors.sumOf { vector ->
						vector.indices.sumOf { dim ->
							val diff = vector[dim] - means[dim] // Разница между значением и средним
							diff * diff // Квадрат разницы
						}
					} / (vectors.size * dimension) // Нормализуем по общему количеству элементов
				}
		}
	}

	fun getVariances() = neurons.map { it.variance }

	fun checkCountOfNeurons() {
		val vectors = Array(n) { mutableListOf<MutableList<Double>>() }
		val vectorIndexes = Array(n) { mutableListOf<Int>() }

		for (i in selection.indices) {
			val index = getNeuronWinner(selection[i], 1)
			neurons[index[0][0]].count += 1
			vectors[index[0][0]].add(selection[i])
			vectorIndexes[index[0][0]].add(i)
		}

		for (i in 0 until n) {
			neurons[i].vectors = vectors[i]
			neurons[i].vectorIndexes = vectorIndexes[i]
		}
	}

	/**
	 * Удаляет нейроны, которые редко активировались.
	 *
	 * @param maxCount - порог для удаления (нейроны с count <= maxCount будут удалены)
	 */
	fun deleteDeadNeurons(maxCount: Int = 500) {
		val aliveNeurons = neurons.filter {
			it.count > maxCount && it.count < 5000
		}
		neurons = ArrayList(aliveNeurons)  // Обновляем список нейронов
		n = neurons.size  // Обновляем количество нейронов
	}

	fun answersCurve(vector: List<Double>, theta: List<Double>): List<Double> {
		val coef = 4
		val curve = mutableListOf<Double>()

		for (th in theta) {
			var summ = vector[0] / sqrt(2.0)
			for (i in 1 until vector.size step 2) {
				summ += vector[i] * sin(i * th / coef - coef * 2)
				if (i + 1 < vector.size) {
					summ += vector[i + 1] * cos(i * th / coef - coef * 2)
				}
			}
			curve.add(summ)
		}

		return curve
	}

	fun magic() {
		// Генерация значений theta от -π до π с шагом 1000
		val theta = (0..1000).map { -Math.PI + (2 * Math.PI * it / 1000) }

		// Создание коллекции серий для графика
		val dataset = XYSeriesCollection()

		for (k in neurons.indices) {
			// Вычисление среднего значения для каждого индекса вектора
			val res = MutableList(neurons[k].vectors[0].size) { 0.0 }
			for (i in neurons[k].vectors[0].indices) {
				res[i] = neurons[k].vectors.sumOf { it[i] } / neurons[k].vectors.size
			}

			// Построение кривой для текущего нейрона
			val curve = answersCurve(res, theta)

			// Создание новой серии для этого нейрона
			val series = XYSeries("Neuron $k")
			curve.forEachIndexed { index, value ->
				series.add(theta[index], value)
			}

			// Добавление серии в набор данных
			dataset.addSeries(series)
		}

		// Создание графика с несколькими сериями
		val chart: JFreeChart = ChartFactory.createXYLineChart(
			"Графики для нейронов", // Заголовок
			"Theta", // Подпись оси X
			"Value", // Подпись оси Y
			dataset
		)

		// Отображение графика в окне
		val chartPanel = ChartPanel(chart)
		val frame = JFrame("График нейронов")
		frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
		frame.add(chartPanel)
		frame.pack()
		frame.isVisible = true
	}
}

fun learn() {
	val nu = 1.0
	val cNeurons = 4
	val alpha = 0.01
	val n = 50
	val network = Network(n)
	network.createForLearn()
	network.learn(nu, cNeurons, alpha)
	network.saveNetwork()
}

fun main() {
	val network = Network()
	network.loadNetwork()
	network.setSelection(DataNormalizer().getLearningSelection())
	network.checkCountOfNeurons()
	network.deleteDeadNeurons()
	network.magic()
}