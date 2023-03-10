# -*- coding: utf-8 -*-
import os
import sys
import inspect
import numpy as np
import time
currentdir = os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe())))
parentdir = os.path.dirname(currentdir)
sys.path.insert(0, parentdir)
from mazelib import Maze
from mazelib.generate.Prims import Prims
# https://stackoverflow.com/a/35352735
from spaceengineers.models import Vec3F, DefinitionId, Vec3I
from spaceengineers.proxy import SpaceEngineersProxy


def read_map(map_name):
    file_path = "./maps/" + map_name + ".csv"

    f = open(file_path, "r")
    lines = f.readlines()
    line_count = 0

    wall_positions = []
    ground_positions = []
    blocks_positions = []

    for line in lines:
        column_count = 0

        line = line.replace('	', '')

        for letter in line.split(";"):
            if letter == '.':
                ground_positions.append((line_count, column_count))
            elif letter == '0':
                # For TESTAR we do not have an objective block.
                # Use this generated objective to automatically place reactor and gravity blocks.
                #ground_positions.append((line_count, column_count))
                blocks_positions.append((line_count, column_count, "gravity"))
            elif letter == '\n':
                pass
            elif letter == ' ':
                pass
            elif letter == '	':
                pass
            elif letter == '_':
                pass
            elif letter == "r":
                ground_positions.append((line_count, column_count))
            elif letter == "m":
                ground_positions.append((line_count, column_count))
            elif letter == 'f':
                ground_positions.append((line_count, column_count))
            elif letter == 'x':
                wall_positions.append((line_count, column_count))
                ground_positions.append((line_count, column_count))
            elif letter == "p":
                ground_positions.append((line_count, column_count))
            # All other characters are length 1, but maybe improve this checking.
            elif len(letter) > 2:
                blocks_positions.append((line_count, column_count, letter))

            column_count += 1
        line_count += 1

    map_matrix = np.zeros((line_count, column_count))
    ground_matrix = np.zeros((line_count, column_count))
    block_matrix = np.zeros((line_count, column_count), dtype=object)

    for pos in wall_positions:
        map_matrix[pos[0]][pos[1]] = 1

    for pos in ground_positions:
        ground_matrix[pos[0]][pos[1]] = 1

    for pos in blocks_positions:
        block_matrix[pos[0]][pos[1]] = pos[2]

    return map_matrix, ground_matrix, block_matrix, line_count, column_count


def generate_maze(map_name):
    LARGE_BLOCK_CUBE_SIDE_SIZE = 2.5

    map_matrix, ground_matrix, block_matrix, height, width = read_map(map_name)
    se = SpaceEngineersProxy.localhost()
    se.Admin.Character.Teleport(position=Vec3F(X=10, Y=10, Z=10))
    definitionId = DefinitionId(Id="MyObjectBuilder_CubeBlock", Type="LargeHeavyBlockArmorBlock")
    gridId = None
    z = 0
    for x in range(0, width):
        for y in range(0, height):
            #if ground_matrix[y][x]:
            if not gridId:
                se.Admin.Blocks.PlaceAt(
                    blockDefinitionId=definitionId,
                    position=Vec3F(
                        X=x * LARGE_BLOCK_CUBE_SIDE_SIZE,
                        Y=y * LARGE_BLOCK_CUBE_SIDE_SIZE,
                        Z=z * LARGE_BLOCK_CUBE_SIDE_SIZE
                    ),
                    orientationUp=Vec3F(X=0, Y=1, Z=0),
                    orientationForward=Vec3F(X=0, Y=0, Z=-1),
                    color=Vec3F(X=0, Y=0, Z=0),
                )

                # The iv4xr ObservationRadius affects this observation
                gridId = se.Observer.ObserveBlocks().Grids[0]["Id"]
            else:
                place_in_grid(definitionId, gridId, se, x, y, z)
    z = -1
    for x in range(0, width):
        for y in range(0, height):
            if map_matrix[y][x]:
                place_in_grid(definitionId, gridId, se, x, y, z)
                place_in_grid(definitionId, gridId, se, x, y, z - 1)
    for x in range(0, width):
        for y in range(0, height):
            if block_matrix[y][x]:
                # For the generated gravity, place a reactor + gravity blocks
                if str(block_matrix[y][x]) == "gravity":
                    block_definition_id = DefinitionId(Id="MyObjectBuilder_Reactor", Type="LargeBlockSmallGenerator")
                    place_in_grid(block_definition_id, gridId, se, x, y, z)
                    block_definition_id = DefinitionId(Id="MyObjectBuilder_GravityGenerator", Type="")
                    place_in_grid(block_definition_id, gridId, se, x, y, z - 1)
                else:
                    block_id = str(block_matrix[y][x]).split("/")[0]
                    block_type = str(block_matrix[y][x]).split("/")[1]
                    block_definition_id = DefinitionId(Id=block_id, Type=block_type)
                    place_in_grid(block_definition_id, gridId, se, x, y, z)


def place_in_grid(definitionId, gridId, se, x, y, z):
    se.Admin.Blocks.PlaceInGrid(
        gridId=gridId,
        blockDefinitionId=definitionId,
        minPosition=Vec3I(X=x, Y=y, Z=z),
        orientationUp=Vec3I(X=0, Y=0, Z=-1),
        orientationForward=Vec3I(X=0, Y=1, Z=0),
        color=Vec3F(X=0, Y=0, Z=0),
    )


if __name__ == '__main__':
    generate_maze("TESTAR_100x100")
