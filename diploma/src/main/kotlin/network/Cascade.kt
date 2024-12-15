package network

import kotlin.math.pow
import kotlin.math.sqrt

class Cascade {

	var baseNetwork = Network()
	val clusters = mutableListOf<Network>()

	fun prepareNetwork(network: Network, maxCount: Int = 500) {
		network.checkCountOfNeurons()
		network.deleteDeadNeurons(maxCount)
		network.calculateVariance()
	}

	fun createClusterArray() {
		loadBaseNetwork()

		var index = 1
		for (cluster in baseNetwork.neurons) {
			val normalizer = DataNormalizer()
			val selection = normalizer.getLearningSelection(cluster.vectorIndexes)
			val network = Network(20)
			network.createForLearnWithSelection(selection)
			network.learn()
			network.calculateVariance()
			network.saveNetwork(file = "./Dataset/cascade/neurons_$index.csv")
			clusters.add(network)
			println("Network $index learned")
			println()
			index += 1
		}
	}

	fun loadBaseNetwork() {
		baseNetwork.setSelection(DataNormalizer().getLearningSelection())
		baseNetwork.loadNetwork()
		prepareNetwork(baseNetwork)
	}

	fun loadClusters() {
		loadBaseNetwork()
		for (index in baseNetwork.neurons.indices) {
			val normalizer = DataNormalizer()
			val network = Network(20)
			val selection = normalizer.getLearningSelection(baseNetwork.neurons[index].vectorIndexes)
			network.setSelection(selection)
			network.loadNetwork(file = "./Dataset/cascade/neurons_${index + 1}.csv")
			network.checkCountOfNeurons()
			network.calculateVariance()
			network.deleteDeadNeurons(100)
			clusters.add(network)
		}
	}

	fun getVar(neuron: Neuron): Double {
		val averagedVector = neuron.vectors[0].indices.map { i ->
			neuron.vectors.sumOf { it[i] } / neuron.vectors.size
		}
		return neuron.vectors.map { vector ->
			lengthBetweenVectors(averagedVector, vector)
		}
			.standardDeviation()
	}

	fun List<Double>.standardDeviation(): Double {
		val mean = this.average()
		return sqrt(this.sumOf { (it - mean).pow(2) } / this.size)
	}


	fun lengthBetweenVectors(arr1: List<Double>, arr2: List<Double>): Double {
		return sqrt(arr1.indices.sumOf { i -> (arr1[i] - arr2[i]).pow(2) })
	}

	fun prepareForVariance() {
		baseNetwork.checkCountOfNeurons()
		val vectorsBase = DataNormalizer().getLearningSelection()

		val variance1 = baseNetwork.neurons.map { neuron -> getVar(neuron) }

		val min1 = variance1.minOrNull() ?: 0.0
		val max1 = variance1.maxOrNull() ?: 0.0
		val sr1 = variance1.average()

		println("1. base network\nmin = $min1\nmax = $max1\navg = $sr1\n")

		val averagedVector = vectorsBase[0].indices.map { i ->
			vectorsBase.sumOf { it[i] } / vectorsBase.size
		}
		val length = vectorsBase.map { vector ->
			lengthBetweenVectors(averagedVector, vector)
		}
		val variance2 = length.standardDeviation()
		println("2. selection\nvar = $variance2$\n")

		val variance3 = clusters.mapNotNull { network ->
			network.neurons.map { neuron -> getVar(neuron) }.average().takeIf { it < 0.13 }
		}

		val min3 = variance3.minOrNull() ?: 0.0
		val max3 = variance3.maxOrNull() ?: 0.0
		val sr3 = variance3.average()

		println("3. clusters\nmin = $min3\nmax = $max3\navg = $sr3\n")
	}

	fun getCountOfUsersInClusters() {
		val variances = clusters.mapNotNull { network ->
			Pair(
				network.neurons.map { neuron -> getVar(neuron) }.average(),
				network.neurons.size
			).takeIf { it.first < 0.13 }
		}
			.groupBy { it.second }
			.mapValues { (_, pairs) ->
				pairs.minByOrNull { it.first }
			}
			.values
			.sortedBy { it?.second }
		println(variances)
	}
}

fun learnCascade() {
	val cascade = Cascade()
	cascade.createClusterArray()
}

fun checkVariance() {
	val cascade = Cascade()
	cascade.loadClusters()
	cascade.prepareForVariance()
}

fun main() {
	val cascade = Cascade()
	cascade.loadClusters()
	cascade.getCountOfUsersInClusters()
}