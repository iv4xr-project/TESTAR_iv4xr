import random

# Constants
LEVEL_SIZE = 50
ROOM_SIZE = 10  # Each room is 10x10
WALL = 'w'
FLOOR = 'f'
BUTTON = 'b'
DOOR = 'd'
AGENT = 'a'

# Initialize the level with walls
level = [[WALL] * LEVEL_SIZE for _ in range(LEVEL_SIZE)]

# Create rooms and their connections
button_door_pairs = []
door_id = 0
agent_placed = False

for room_row in range(0, LEVEL_SIZE, ROOM_SIZE):
    for room_col in range(0, LEVEL_SIZE, ROOM_SIZE):
        # Create floors inside the room
        for i in range(room_row + 1, room_row + ROOM_SIZE - 1):
            for j in range(room_col + 1, room_col + ROOM_SIZE - 1):
                level[i][j] = FLOOR
        
        # Place the agent in the first room
        if not agent_placed:
            level[room_row + 1][room_col + 1] = f"{FLOOR}:{AGENT}^agent1"
            agent_placed = True
        
        # Place a button in the room
        button_pos = (room_row + random.randint(1, ROOM_SIZE - 2), room_col + random.randint(1, ROOM_SIZE - 2))
        button_id = len(button_door_pairs)
        level[button_pos[0]][button_pos[1]] = f"{FLOOR}:{BUTTON}^button{button_id}"

        # Connect rooms with doors (one horizontal or vertical connection per room)
        if room_col + ROOM_SIZE < LEVEL_SIZE:  # Create a door to the right room
            mid_row = room_row + ROOM_SIZE // 2
            level[mid_row][room_col + ROOM_SIZE - 1] = FLOOR  # Remove the wall for the door
            level[mid_row][room_col + ROOM_SIZE] = f"{FLOOR}:{DOOR}>e^door{door_id}"
            button_door_pairs.append((f"button{button_id}", f"door{door_id}"))
            door_id += 1

        if room_row + ROOM_SIZE < LEVEL_SIZE:  # Create a door to the bottom room
            mid_col = room_col + ROOM_SIZE // 2
            level[room_row + ROOM_SIZE - 1][mid_col] = FLOOR  # Remove the wall for the door
            level[room_row + ROOM_SIZE][mid_col] = f"{FLOOR}:{DOOR}>s^door{door_id}"
            button_door_pairs.append((f"button{button_id}", f"door{door_id}"))
            door_id += 1

# Print button-door pairs
for pair in button_door_pairs:
    print(f"{pair[0]},{pair[1]}")

# Print the level in the desired format
print(f"|{','.join(level[0])}")
for row in level[1:]:
    print(",".join(row))
    
# Print the big walls
print(f"|{','.join(level[0])}")
for row in level[1:]:
    print(",".join(cell if cell == WALL else "" for cell in row))

