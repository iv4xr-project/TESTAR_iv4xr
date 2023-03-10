import random
from os.path import exists
import os
import numpy as np
import math
from collections import defaultdict

"""
Idea: make a map generator based on the concept of the turtle drawing library. 
We have a map generator that goes along the map, making corridors as it goes and sometimes expanding them into rooms of different sizes.
It also places coins and enemies along the way

Based on:
https://github.com/iv4xr-project/PAD_emotion_game/blob/main/map_generator.py
"""

class TurtleGenerator(object):

	directions = [(-1,0), (1,0), (0,1), (0,-1)]

	def __init__(self, canvas_size, rotation_frequency):

		self.map = []
		for _ in range(canvas_size):
			line = ['_']*canvas_size
			self.map.append(line)
		print(self.map)


class TopDownGenerator(object):
	pass


class BottomUpGenerator(object):
	floor_char = '.'
	empty_char = '_'
	wall_char = 'x'
	player_char = 'p'
	gravity_char = 'g'
	block_char = 'b'
	objective_char = '0'

	save_folder = "./GeneratedMaps/"

	def __init__(self, canvas_size, dungeon_density, num_loops, neighbour_depth, neighbour_number_threshold, gravity_density, block_density, minimum_tile_distance_player_flower):
		self.map = []
		self.canvas_size = canvas_size
		self.dungeon_density = dungeon_density
		self.num_loops = num_loops
		self.neighbour_depth = neighbour_depth
		self.neighbour_number_threshold = neighbour_number_threshold
		self.gravity_density = gravity_density
		self.block_density = block_density
		self.minimum_distance_player_flower = minimum_tile_distance_player_flower
		self.small_col_matrix = None


	def make_a_map(self, verbose = False, name = None):
		path_between_player_and_flower = []

		tries = 0

		while len(path_between_player_and_flower) < self.minimum_distance_player_flower/2:

			tries += 1

			if tries > 500:
				print("Cannot find viable map for given parameters after 500 tries. Giving up...")
				return False

			self.map = []

			self.initialize_empty_map()

			if verbose:
				self.print_map("SIZE")

			self.randomly_add_floor_tiles()

			if verbose:
				self.print_map("FLOOR")

			self.aglutinate_floor()

			self.wall_it_up()

			if verbose:
				self.print_map("WALLS")

			np.set_printoptions(threshold=np.inf)
			self.makeCollisionMatrix()

			player_pos = self.place_player_randomly()

			if verbose:
				self.print_map("PLAYER")

			flower_pos = self.place_flower_randomly()

			if verbose:
				self.print_map("FLOWER")

			path_between_player_and_flower = self.AStar((int(player_pos[0]/2), int(player_pos[1]/2)), (int(flower_pos[0]/2), int(flower_pos[1]/2)))


		self.place_gravity_randomly()
		self.place_blocks_randomly()

		if verbose:
			self.print_map("BLOCKS")

		self.replace_gravity()
		self.replace_blocks()

		if verbose:
			self.print_map("TYPES")

		self.save_as_csv(name = name)


	def print_map(self, placed):
		print("--------MAP--------")
		print("***** " + placed + " *****")

		for line in self.map:
			for char in line:
				print(char, end = '')
			print()


	def initialize_empty_map(self):
		for _ in range(self.canvas_size):
			line = ['_']*self.canvas_size
			self.map.append(line)


	def randomly_add_floor_tiles(self):
		for i in range(self.canvas_size):
			for j in range(self.canvas_size):
				if random.randint(0,100) < self.dungeon_density:
					self.map[i][j] = self.floor_char


	def get_position_empty_neighbours(self, i, j):
		neighbours = []

		if self.neighbour_depth <= 0:
			print("Depth variable needs to be greater than 0")
			exit()

		for n_i in range(i - 1, i + 1 + 1):
			for n_j in range(j - 1, j + 1 + 1):
				if n_i == i and n_j == j:
					continue
				if self.map[n_i][n_j] == self.empty_char:
					neighbours.append([n_i, n_j])

		return neighbours


	def get_position_floored_neighbours(self, i, j):
		neighbours = []
		bordering_edges = False

		if self.neighbour_depth <= 0:
			print("Depth variable needs to be greater than 0")
			exit()

		for n_i in range(i - self.neighbour_depth, i + self.neighbour_depth + 1):
			for n_j in range(j - self.neighbour_depth, j + self.neighbour_depth + 1):


				if n_i < 0 or n_i >= self.canvas_size or n_j < 0 or n_j >= self.canvas_size:
					bordering_edges = True
					continue
				if n_i == i and n_j == j:
					continue
				if self.map[n_i][n_j] == self.floor_char:
					neighbours.append([n_i, n_j])

		return neighbours, bordering_edges


	def aglutinate_floor(self):
		for run in range(self.num_loops):
			to_add = []
			to_remove = []
			for i in range(self.canvas_size):
				for j in range(self.canvas_size):
					neighbors, bordering = self.get_position_floored_neighbours(i, j)
					if (len(neighbors) >= self.neighbour_number_threshold) and not bordering:
						to_add.append([i, j])
					else:
						to_remove.append([i,j])
			for cord in to_add:
				self.map[cord[0]][cord[1]] = self.floor_char
			for cord in to_remove:
				self.map[cord[0]][cord[1]] = self.empty_char


	def wall_it_up(self):
		for i in range(self.canvas_size):
			for j in range(self.canvas_size):
				if self.map[i][j] == self.floor_char:
					empty_neighbours = self.get_position_empty_neighbours(i,j)
					for neighbour in empty_neighbours:
						self.map[neighbour[0]][neighbour[1]] = self.wall_char


	def place_player_randomly(self):
		for _ in range(10000):
			i = random.randint(1, self.canvas_size-2)
			j = random.randint(1, self.canvas_size-2)
			if self.map[i][j] == self.floor_char and len(self.get_position_floored_neighbours(i,j)[0]) == 8:
				self.map[i][j] = self.player_char
				return (i,j)

		print("Too many attempts at placing player. The map generator quit...")
		return False


	def place_flower_randomly(self):
		for _ in range(10000):
			i = random.randint(1, self.canvas_size-2)
			j = random.randint(1, self.canvas_size-2)
			if self.map[i][j] == self.floor_char and len(self.get_position_floored_neighbours(i,j)[0]) == 8:
				self.map[i][j] = self.objective_char
				return (i,j)

		print("Too many attempts at placing flower. The map generator quit...")
		return False

	def place_gravity_randomly(self):
		for _ in range(self.canvas_size*self.canvas_size):
			i = random.randint(1, self.canvas_size-2)
			j = random.randint(1, self.canvas_size-2)
			if self.map[i][j] == self.floor_char and len(self.get_position_floored_neighbours(i,j)[0]) == 8:
				if random.uniform(0.0, 100.0) < self.gravity_density:
					self.map[i][j] = self.gravity_char


	def replace_gravity(self):
		for i in range(self.canvas_size):
			for j in range(self.canvas_size):
				if self.map[i][j] == self.gravity_char:
					self.map[i][j] = "gravity"


	def place_blocks_randomly(self):
		for _ in range(self.canvas_size*self.canvas_size):
			i = random.randint(1, self.canvas_size-2)
			j = random.randint(1, self.canvas_size-2)
			if self.map[i][j] == self.floor_char and len(self.get_position_floored_neighbours(i,j)[0]) == 8:
				if random.uniform(0.0, 100.0) < self.block_density:
					self.map[i][j] = self.block_char


	def replace_blocks(self):
		for i in range(self.canvas_size):
			for j in range(self.canvas_size):
				if self.map[i][j] == self.block_char:
					self.map[i][j] = random.choice(block_types_list)


	def makeCollisionMatrix(self):
		self.small_col_matrix = np.zeros((int(self.canvas_size/2), int(self.canvas_size/2)))

		for i in range(int(self.canvas_size/2)):
			for j in range(int(self.canvas_size/2)):
				if self.map[i*2][j*2] == self.wall_char or self.map[i*2 + 1][j*2] == self.wall_char or self.map[i*2][j*2 + 1] == self.wall_char or self.map[i*2 + 1][j*2 + 1] == self.wall_char:
					self.small_col_matrix[i][j] = 1


	def reconstruct_path(self, cameFrom, current, start):
		total_path = [current]
		while current != start:
			current = cameFrom[current]
			total_path.insert(0, current)
		return total_path


	def AStarHeuristic(self, a, b):
		#Euclidian Distance
		return ((a[0] - b[0])**2 + (a[1] - b[1])**2)**0.5


	#Only returns diagonals when both adjacent horizontal and vertical tiles are also free as to avoid collisions.
	def getMixedNeighbors(self, node):
		neighbors = []
		
		if node[0]-1 > 0 and self.small_col_matrix[node[0]-1][node[1]] == 0:
			neighbors.append((node[0]-1, node[1]))
			if node[1]-1 > 0 and self.small_col_matrix[node[0]][node[1]-1] == 0:
				if self.small_col_matrix[node[0]-1][node[1]-1] == 0:
					neighbors.append((node[0]-1, node[1]-1))
			if node[1]+1 < len(self.small_col_matrix[0]) and self.small_col_matrix[node[0]][node[1]+1] == 0:
				if self.small_col_matrix[node[0]-1][node[1]+1] == 0:
					neighbors.append((node[0]-1, node[1]+1))
		if node[0]+1 < len(self.small_col_matrix) and self.small_col_matrix[node[0]+1][node[1]] == 0:
			neighbors.append((node[0] + 1, node[1]))
			if node[1]-1 > 0 and self.small_col_matrix[node[0]][node[1]-1] == 0:
				if self.small_col_matrix[node[0]+1][node[1]-1] == 0:
					neighbors.append((node[0]+1, node[1]-1))
			if node[1]+1 < len(self.small_col_matrix[0]) and self.small_col_matrix[node[0]][node[1]+1] == 0:
				if self.small_col_matrix[node[0]+1][node[1]+1] == 0:
					neighbors.append((node[0]+1, node[1]+1))
		if node[1]-1 > 0 and self.small_col_matrix[node[0]][node[1]-1] == 0:
			neighbors.append((node[0], node[1]-1))
		if node[1]+1 < len(self.small_col_matrix[0]) and self.small_col_matrix[node[0]][node[1]+1] == 0:
			neighbors.append((node[0], node[1] + 1))

		return neighbors

	# A* finds a path from start to goal.
	# h is the heuristic function. h(n) estimates the cost to reach goal from node n.
	def AStar(self, start, goal):
		# The set of discovered nodes that may need to be (re-)expanded.
		# Initially, only the start node is known.
		# This is usually implemented as a min-heap or priority queue rather than a hash-set.
		openSet = [start]

		# For node n, cameFrom[n] is the node immediately preceding it on the cheapest path from start
		# to n currently known.
		cameFrom = {}

		# For node n, gScore[n] is the cost of the cheapest path from start to n currently known.
		# I initialize every value as Infinite
		gScore = {}
		gScore = defaultdict(lambda: math.inf, gScore)
		gScore[start] = 0

		# For node n, fScore[n] := gScore[n] + h(n). fScore[n] represents our current best guess as to
		# how short a path from start to finish can be if it goes through n.
		fScore = {}
		fScore = defaultdict(lambda: math.inf, fScore)
		fScore[start] = self.AStarHeuristic(start, goal)

		while len(openSet) > 0:
			# This operation can occur in O(Log(N)) time if openSet is a min-heap or a priority queue
			min_val = math.inf
			for node in openSet:
				if fScore[node] < min_val:
					min_val = fScore[node]
					current = node

			if current == goal:
				return self.reconstruct_path(cameFrom, current, start)

			openSet.remove(current)

			for neighbor in self.getMixedNeighbors(current):
				# d(current,neighbor) is the weight of the edge from current to neighbor
				# tentative_gScore is the distance from start to the neighbor through current
				tentative_gScore = gScore[current] + 1 #d(current, neighbor) --> for now, lets say all weights are 1
				if tentative_gScore < gScore[neighbor]:
					# This path to neighbor is better than any previous one. Record it!
					cameFrom[neighbor] = current
					gScore[neighbor] = tentative_gScore
					fScore[neighbor] = tentative_gScore + self.AStarHeuristic(neighbor, goal)
					if neighbor not in openSet:
						openSet.append(neighbor)

		# Open set is empty but goal was never reached
		return []


	def save_as_csv(self, name):
		rand_id = random.randint(1000000000, 9999999999)

		if name == None:
			file_path = self.save_folder + "BottomUpMap_" + str(self.canvas_size) + "_" + str(self.dungeon_density) + "_" + str(self.num_loops) + "_" + str(self.neighbour_depth) + "_" + str(self.neighbour_number_threshold) + "_" + str(rand_id) +".csv"
		else:
			file_path = self.save_folder + "Map_" + str(name) +".csv"


		while exists(file_path):
			if name == None:
				print("File already exists")
				return
			else:
				rand_id = random.randint(1000000000, 9999999999)
				file_path = self.save_folder + "BottomUpMap_" + str(self.canvas_size) + "_" + str(self.dungeon_density) + "_" + str(self.num_loops) + "_" + str(self.neighbour_depth) + "_" + str(self.neighbour_number_threshold) + "_" + str(rand_id) +".csv"

		f = open(file_path, 'w+')

		for line in self.map:
			for char in line:
				f.write(char)
				f.write(';')
			f.write("\n")


# canvas_size, dungeon_density, num_loops, neighbour_depth, neighbour_number_threshold, gravity_density, block_density, minimum_tile_distance_player_flower
turly = BottomUpGenerator(102, 30, 3, 1, 3, 2, 5, 10)

# Define the types of blocks that are generated automatically
# gravity will be interpreted such as a combination of LargeBlockSmallGenerator + GravityGenerator
block_types_list = ["MyObjectBuilder_BatteryBlock/LargeBlockBatteryBlock",
"MyObjectBuilder_CryoChamber/LargeBlockCryoChamber",
"MyObjectBuilder_SurvivalKit/SurvivalKitLarge",
"MyObjectBuilder_Cockpit/LargeBlockCockpitSeat",
"MyObjectBuilder_ConveyorConnector/ConveyorTubeCurved",
"MyObjectBuilder_CargoContainer/LargeBlockSmallContainer"]

for i in range(1):
	turly.make_a_map(name = i, verbose = True)

