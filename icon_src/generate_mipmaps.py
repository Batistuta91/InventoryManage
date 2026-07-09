from PIL import Image, ImageDraw
import os

SRC = "icon_src/icon_1024.png"
img = Image.open(SRC).convert("RGBA")

# Standard launcher icon sizes (legacy, square/round)
sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

# Foreground sizes for adaptive icons (108dp square, larger canvas so safe-zone crops don't clip)
adaptive_sizes = {
    "mipmap-mdpi": 108,
    "mipmap-hdpi": 162,
    "mipmap-xhdpi": 216,
    "mipmap-xxhdpi": 324,
    "mipmap-xxxhdpi": 432,
}

base_res = "android/app/src/main/res"

def make_round(im):
    size = im.size[0]
    mask = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(mask)
    d.ellipse([0, 0, size, size], fill=255)
    out = Image.new("RGBA", (size, size), (0,0,0,0))
    out.paste(im, (0,0), mask)
    return out

for folder, size in sizes.items():
    d = os.path.join(base_res, folder)
    os.makedirs(d, exist_ok=True)
    resized = img.resize((size, size), Image.LANCZOS)
    resized.save(os.path.join(d, "ic_launcher.png"))
    make_round(resized).save(os.path.join(d, "ic_launcher_round.png"))
    resized.save(os.path.join(d, "ic_launcher_foreground.png"))

# Adaptive icon foreground: place original art scaled onto a larger transparent canvas
# so it's not clipped by the adaptive icon mask (safe zone ~66% of canvas).
for folder, size in adaptive_sizes.items():
    d = os.path.join(base_res, folder)
    os.makedirs(d, exist_ok=True)
    canvas = Image.new("RGBA", (size, size), (0,0,0,0))
    inner = int(size * 0.72)
    art = img.resize((inner, inner), Image.LANCZOS)
    offset = ((size - inner)//2, (size - inner)//2)
    canvas.paste(art, offset, art)
    canvas.save(os.path.join(d, "ic_launcher_foreground.png"))

# Play Store hi-res icon
img.resize((512,512), Image.LANCZOS).save("icon_src/playstore_icon_512.png")

print("done")
