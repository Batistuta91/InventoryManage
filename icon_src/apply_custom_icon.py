from PIL import Image, ImageDraw
import os

SRC = "icon_src/custom_icon_src.png"
im = Image.open(SRC).convert("RGBA")

# Crop to a perfect square (center crop) in case of slight aspect mismatch
w, h = im.size
side = min(w, h)
left = (w - side) // 2
top = (h - side) // 2
im = im.crop((left, top, left + side, top + side))
im = im.resize((1024, 1024), Image.LANCZOS)
im.save("icon_src/icon_1024.png")

sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}
adaptive_sizes = {
    "mipmap-mdpi": 108,
    "mipmap-hdpi": 162,
    "mipmap-xhdpi": 216,
    "mipmap-xxhdpi": 324,
    "mipmap-xxxhdpi": 432,
}

base_res = "android/app/src/main/res"

def make_round(img):
    size = img.size[0]
    mask = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(mask)
    d.ellipse([0, 0, size, size], fill=255)
    out = Image.new("RGBA", (size, size), (0,0,0,0))
    out.paste(img, (0,0), mask)
    return out

for folder, size in sizes.items():
    d = os.path.join(base_res, folder)
    os.makedirs(d, exist_ok=True)
    resized = im.resize((size, size), Image.LANCZOS)
    resized.save(os.path.join(d, "ic_launcher.png"))
    make_round(resized).save(os.path.join(d, "ic_launcher_round.png"))

# Adaptive icon foreground: scale the full image down a bit onto a transparent
# canvas so the circuit-board edges aren't clipped by the adaptive mask's safe zone.
for folder, size in adaptive_sizes.items():
    d = os.path.join(base_res, folder)
    os.makedirs(d, exist_ok=True)
    canvas = Image.new("RGBA", (size, size), (0,0,0,0))
    inner = int(size * 0.80)
    art = im.resize((inner, inner), Image.LANCZOS)
    offset = ((size - inner)//2, (size - inner)//2)
    canvas.paste(art, offset, art)
    canvas.save(os.path.join(d, "ic_launcher_foreground.png"))

im.resize((512,512), Image.LANCZOS).save("icon_src/playstore_icon_512.png")
print("done")
