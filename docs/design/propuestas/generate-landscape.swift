// Reproduces the original CSS sky and SVG paths in minimalistas.html, without demo UI.
// Run: swift docs/design/propuestas/generate-landscape.swift <output.png>
import AppKit
let width = 1920, height = 1080
let space = CGColorSpaceCreateDeviceRGB()
let context = CGContext(data: nil, width: width, height: height, bitsPerComponent: 8,
    bytesPerRow: width * 4, space: space, bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue)!
let pixels = context.data!.bindMemory(to: UInt8.self, capacity: width * height * 4)
let stops: [(Double, [Double])] = [(0, [244,206,163]), (0.2,[190,170,159]), (0.5,[92,124,140]), (0.9,[38,62,82])]
for y in 0..<height { for x in 0..<width {
    let radius = sqrt(pow((Double(x) - 1440) / 1500, 2) + pow((Double(y) - 432) / 1000, 2))
    let upper = stops.firstIndex { $0.0 >= radius } ?? 3
    let lower = max(0, upper - 1)
    let fraction = upper == lower ? 0 : min(1, (radius - stops[lower].0) / (stops[upper].0 - stops[lower].0))
    let overlay = Double(y) / 1080 * 0.788
    for channel in 0..<3 {
        let sky = stops[lower].1[channel] * (1 - fraction) + stops[upper].1[channel] * fraction
        pixels[(y * width + x) * 4 + channel] = UInt8(sky * (1-overlay) + [16.0,45.0,57.0][channel] * overlay)
    }
    pixels[(y * width + x) * 4 + 3] = 255
}}
// SVG coordinates are top-down.
context.translateBy(x: 0, y: 1080); context.scaleBy(x: 1, y: -1)
func ridge(_ start: CGPoint, _ curves: [(CGPoint, CGPoint)], _ color: [CGFloat]) {
    context.beginPath(); context.move(to: start)
    for (control, end) in curves { context.addQuadCurve(to: end, control: control) }
    context.addLine(to: CGPoint(x:1920,y:1080)); context.addLine(to: CGPoint(x:0,y:1080)); context.closePath()
    context.setFillColor(CGColor(colorSpace: space, components: color)!); context.fillPath()
}
ridge(CGPoint(x:0,y:610), [(CGPoint(x:220,y:330),CGPoint(x:500,y:570)),(CGPoint(x:780,y:810),CGPoint(x:1100,y:480)),(CGPoint(x:1420,y:150),CGPoint(x:1920,y:420))], [123/255,147/255,151/255,0.5])
ridge(CGPoint(x:0,y:820), [(CGPoint(x:350,y:520),CGPoint(x:750,y:750)),(CGPoint(x:1150,y:980),CGPoint(x:1400,y:600)),(CGPoint(x:1650,y:220),CGPoint(x:1920,y:700))], [57/255,94/255,106/255,1])
ridge(CGPoint(x:0,y:1040), [(CGPoint(x:300,y:760),CGPoint(x:780,y:980)),(CGPoint(x:1260,y:1200),CGPoint(x:1500,y:880)),(CGPoint(x:1740,y:560),CGPoint(x:1920,y:930))], [19/255,51/255,63/255,0.9])
let image = NSBitmapImageRep(cgImage: context.makeImage()!)
try image.representation(using: .png, properties: [:])!.write(to: URL(fileURLWithPath: CommandLine.arguments[1]))
