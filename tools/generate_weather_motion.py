"""Generate original, offline weather loops. Run with Python + Pillow.

No downloaded artwork. Each loop is sampled at 32 steps (2.24 seconds), a transparent
background and a matching PNG for reduced motion. Kotlin resource mapping lives
in ui/weather/WeatherMotion.kt. Regenerate assets after editing this source.
"""
from pathlib import Path
from math import sin, cos, pi
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1] / 'app/src/main/res'
SIZE, SCALE, FRAMES = 192, 3, 32
INK = '#5B6688'

def frame(kind, phase):
    image = Image.new('RGBA', (SIZE*SCALE, SIZE*SCALE))
    draw = ImageDraw.Draw(image)
    def box(coords): return tuple(round(v*SCALE) for v in coords)
    def circle(x, y, r, fill, outline=None, width=2):
        draw.ellipse(box((x-r,y-r,x+r,y+r)), fill=fill, outline=outline, width=width*SCALE)
    def line(points, fill, width=3):
        draw.line([tuple(round(v*SCALE) for v in p) for p in points], fill=fill, width=width*SCALE)
    wave = sin(phase*2*pi)
    if kind in ('sun', 'sunrise', 'sunset'):
        y = 91 + wave*4
        if kind != 'sun': y = 100 + wave*9
        circle(96,y,37,'#FFD36C','#F2A746',2)
        circle(88,y-10,23,'#FFDF85')
        for i in range(10):
            a = 2*pi*i/10 + phase*2*pi/10
            line([(96+49*cos(a),y+49*sin(a)),(96+61*cos(a),y+61*sin(a))], '#F4AF46', 4)
        if kind != 'sun':
            draw.rectangle(box((20,123,172,180)), fill=(0,0,0,0))
            line([(24,126),(168,126)], '#BC815E', 3)
            for i in range(3):
                half=24-i*6+wave*2
                line([(96-half,138+i*9),(96+half,138+i*9)], '#E5AD74', 2)
    elif kind == 'night':
        y=88+wave*3
        circle(94,y,40,'#CBC8F8')
        circle(112,y-15,36,(0,0,0,0))
        for x, sy, offset in [(136,55,0),(146,105,1),(56,45,2)]:
            r=3+(sin(phase*2*pi+offset)+1)
            line([(x-r,sy),(x+r,sy)], '#BDB4ED',2)
            line([(x,sy-r),(x,sy+r)], '#BDB4ED',2)
    else:
        y=88+wave*3
        circle(115,y-25,25,'#FFE09A')
        # A single filled cloud silhouette with a subtle lower edge.
        draw.rounded_rectangle(box((37,y-5,157,y+35)),radius=20*SCALE,fill='#C7D6E6')
        circle(75,y-5,27,'#EEF5FA')
        circle(112,y-12,34,'#EEF5FA')
        draw.rounded_rectangle(box((37,y,157,y+29)),radius=15*SCALE,fill='#EEF5FA')
        if kind == 'rain':
            for i,x in enumerate((63,92,121)):
                progress=(phase+i/3)%1
                yy=128+progress*28
                line([(x+3,yy),(x-2,yy+10)], '#74A6D2',3)
        elif kind == 'snow':
            for i,x in enumerate((60,96,133)):
                yy=132+((phase+i/3)%1)*24
                xx=x+sin(phase*2*pi+i)*4
                line([(xx-4,yy),(xx+4,yy)],'#9DBDD7',2)
                line([(xx,yy-4),(xx,yy+4)],'#9DBDD7',2)
        elif kind == 'storm':
            draw.polygon([box(p) for p in ((101,119),(84,145),(98,145),(90,166),(116,136),(101,136))],fill='#F4BC55')
        elif kind == 'fog':
            for i in range(3):
                offset=wave*(4+i)
                line([(50+offset,135+i*10),(141+offset,135+i*10)],'#A8B7C8',3)
    return image.resize((SIZE,SIZE),Image.Resampling.LANCZOS)

for kind in ('sun','cloud','rain','snow','storm','fog','night','sunrise','sunset'):
    frames=[]
    first=None
    for n in range(FRAMES):
        rgba=frame(kind,n/FRAMES)
        if n == 0: first=rgba
        # Reserve index zero for transparency; all opaque pixels use 1..255.
        rgb=Image.new('RGB',rgba.size,'#FAFAFA')
        rgb.paste(rgba,mask=rgba.getchannel('A'))
        quantized=rgb.quantize(colors=254)
        palette=quantized.getpalette()
        indexed=Image.new('P',rgba.size)
        indexed.putpalette([0,0,0]+palette[:762])
        alpha=rgba.getchannel('A')
        indexed.putdata([pixel+1 if a>=100 else 0 for pixel,a in zip(quantized.tobytes(),alpha.tobytes())])
        indexed.info['transparency']=0
        frames.append(indexed)
    if kind == 'sunrise':
        first.save(ROOT/'drawable-nodpi'/'motion_horizon_still.png')
    elif kind != 'sunset':
        first.save(ROOT/'drawable-nodpi'/f'motion_{kind}_still.png')
    frames[0].save(ROOT/'raw'/f'motion_{kind}.gif',save_all=True,append_images=frames[1:],
                   duration=70,loop=0,transparency=0,disposal=2,optimize=False)
    print(kind, (ROOT/'raw'/f'motion_{kind}.gif').stat().st_size)
