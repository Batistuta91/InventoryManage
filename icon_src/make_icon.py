from PIL import Image, ImageDraw
import math

SIZE = 1024
img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Diagonal gradient background (matches app header gradient #4e4376 -> #2b5876)
c1 = (78, 67, 118)    # #4e4376
c2 = (43, 88, 118)    # #2b5876
for y in range(SIZE):
    for_row = y / SIZE
    r = int(c1[0] + (c2[0]-c1[0]) * for_row)
    g = int(c1[1] + (c2[1]-c1[1]) * for_row)
    b = int(c1[2] + (c2[2]-c1[2]) * for_row)
    draw.line([(0, y), (SIZE, y)], fill=(r, g, b, 255))

# Rounded square mask for a soft app-icon card look (full bleed background is fine;
# adaptive icon system will handle masking on Android, but keep some inset content)
mask = Image.new("L", (SIZE, SIZE), 0)
mdraw = ImageDraw.Draw(mask)
mdraw.rounded_rectangle([0, 0, SIZE, SIZE], radius=180, fill=255)
bg = Image.new("RGBA", (SIZE, SIZE), (0,0,0,0))
bg.paste(img, (0,0), mask)
img = bg
draw = ImageDraw.Draw(img)

# White "calendar / chip" card in the center
card_margin = 230
card_top = 260
card_bottom = 800
draw.rounded_rectangle(
    [card_margin, card_top, SIZE - card_margin, card_bottom],
    radius=40, fill=(255, 255, 255, 255)
)

# Calendar top tab (small colored strip)
tab_h = 70
draw.rounded_rectangle(
    [card_margin, card_top, SIZE - card_margin, card_top + tab_h],
    radius=40, fill=(230, 243, 255, 255)
)
draw.rectangle([card_margin, card_top + 30, SIZE - card_margin, card_top + tab_h], fill=(230, 243, 255, 255))

# Two "calendar rings" at the top
ring_w = 18
ring_h = 60
for cx in (SIZE//2 - 140, SIZE//2 + 140):
    draw.rounded_rectangle(
        [cx - ring_w//2, card_top - ring_h//2, cx + ring_w//2, card_top + ring_h//2],
        radius=9, fill=(78, 67, 118, 255)
    )

# Barcode-style lines inside the card (representing the date/lot code)
bar_top = card_top + tab_h + 70
bar_bottom = bar_top + 220
bar_left = card_margin + 70
bar_right = SIZE - card_margin - 70
import random
random.seed(7)
x = bar_left
bar_colors = [(43, 88, 118, 255), (78, 67, 118, 255)]
i = 0
while x < bar_right:
    w = random.choice([10, 10, 18, 26, 10])
    draw.rectangle([x, bar_top, x + w, bar_bottom], fill=bar_colors[i % 2])
    x += w + 14
    i += 1

# Date text block below barcode (three small pill shapes to represent day/month/year)
pill_y = bar_bottom + 60
pill_h = 70
pill_w = 130
gap = 40
total_w = pill_w * 3 + gap * 2
start_x = SIZE//2 - total_w//2
colors = [(230,243,255,255), (255,230,230,255), (243,230,255,255)]
for idx in range(3):
    x0 = start_x + idx * (pill_w + gap)
    draw.rounded_rectangle([x0, pill_y, x0 + pill_w, pill_y + pill_h], radius=20, fill=colors[idx])

img.save("icon_src/icon_1024.png")
print("saved")
