#!/usr/bin/env python3
"""
Generate simple placeholder textures for the ocean side-set items.
Requires: pip install pillow

Usage: python generate_textures.py
This creates 16x16 PNG textures in src/main/resources/assets/testmod/textures/item/
"""

from PIL import Image, ImageDraw
import os

# Create textures directory
texture_dir = "src/main/resources/assets/testmod/textures/item"
os.makedirs(texture_dir, exist_ok=True)

def create_solid_texture(filename, color, size=16):
    """Create a solid-color texture."""
    img = Image.new('RGBA', (size, size), color)
    img.save(os.path.join(texture_dir, filename))
    print(f"Created {filename}")

def create_pattern_texture(filename, base_color, pattern_color, size=16):
    """Create a texture with a simple checkerboard-like pattern."""
    img = Image.new('RGBA', (size, size), base_color)
    draw = ImageDraw.Draw(img)
    
    # Add a simple pattern
    for i in range(0, size, 2):
        for j in range(0, size, 2):
            if (i // 2 + j // 2) % 2 == 0:
                draw.rectangle([i, j, i+2, j+2], fill=pattern_color)
    
    img.save(os.path.join(texture_dir, filename))
    print(f"Created {filename}")

def create_striped_texture(filename, color1, color2, size=16):
    """Create a striped texture."""
    img = Image.new('RGBA', (size, size), color1)
    draw = ImageDraw.Draw(img)
    
    for i in range(0, size, 2):
        draw.line([(i, 0), (i, size)], fill=color2, width=1)
    
    img.save(os.path.join(texture_dir, filename))
    print(f"Created {filename}")

# Generate textures
# Bamboo Respirator Helmet: green (bamboo) + cyan (water) pattern
create_pattern_texture(
    "bamboo_respirator_helmet.png",
    base_color=(34, 139, 34, 255),      # Forest green (bamboo)
    pattern_color=(0, 200, 255, 255)    # Cyan (water)
)

# Crop-Copper Backtank: copper orange + kelp brown stripes
create_striped_texture(
    "crop_copper_backtank_chestplate.png",
    color1=(184, 115, 51, 255),         # Copper color
    color2=(101, 67, 33, 255)           # Kelp brown
)

# Straw Flippers: straw yellow + water blue
create_pattern_texture(
    "straw_flippers.png",
    base_color=(230, 190, 80, 255),     # Straw yellow
    pattern_color=(64, 164, 223, 255)   # Water blue
)

print("\nTextures generated successfully!")
print(f"Files created in: {texture_dir}")
print("\nNote: These are placeholder textures.")
print("For better visuals, create or find custom 16x16 textures in a pixel art editor.")
