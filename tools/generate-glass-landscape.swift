// Original procedural artwork, no downloaded images. Run: swift tools/generate-glass-landscape.swift
import AppKit
import CoreImage

let width = 1600, height = 900
let colorSpace = CGColorSpaceCreateDeviceRGB()
let context = CGContext(data: nil, width: width, height: height, bitsPerComponent: 8,
                        bytesPerRow: width * 4, space: colorSpace,
                        bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue)!
func color(_ hex: UInt32, _ alpha: CGFloat = 1) -> CGColor {
    CGColor(red: CGFloat((hex >> 16) & 255) / 255, green: CGFloat((hex >> 8) & 255) / 255,
            blue: CGFloat(hex & 255) / 255, alpha: alpha)
}
func gradient(_ colors: [CGColor], from: CGPoint, to: CGPoint) {
    context.drawLinearGradient(CGGradient(colorsSpace: colorSpace, colors: colors as CFArray, locations: nil)!,
                               start: from, end: to, options: [.drawsBeforeStartLocation, .drawsAfterEndLocation])
}
gradient([color(0x14365c), color(0x677fa8), color(0xc4a8b8), color(0xf4b4a3)],
         from: CGPoint(x: 0, y: 900), to: CGPoint(x: 0, y: 310))
// A translucent, atmospheric planet, not a logo or a borrowed image.
context.saveGState()
context.addEllipse(in: CGRect(x: 1030, y: 615, width: 390, height: 390)); context.clip()
gradient([color(0xced9e7, 0.22), color(0xe7b9b9, 0.04)], from: CGPoint(x: 1060, y: 950), to: CGPoint(x: 1350, y: 620))
context.restoreGState()
context.setStrokeColor(color(0xf0d4da, 0.15)); context.setLineWidth(1.5)
context.strokeEllipse(in: CGRect(x: 1030, y: 615, width: 390, height: 390))

func ridge(_ x: Double, _ layer: Int) -> Double {
    let phase = Double(layer) * 1.9
    let broad = abs(sin(x * 0.005 + phase)) * 125 + abs(sin(x * 0.012 + phase)) * 68
    let detail = sin(x * 0.043 + phase) * 10 + sin(x * 0.113) * 4 + sin(x * 0.23) * 2
    return 320 + Double(layer) * 14 + broad + detail
}
var seed: UInt64 = 0xC0A14A6E
func random() -> Double {
    seed = seed &* 6364136223846793005 &+ 1442695040888963407
    return Double(seed >> 11) / Double(UInt64.max >> 11)
}
for layer in (0...3).reversed() {
    let path = CGMutablePath(); path.move(to: CGPoint(x: 0, y: 240))
    for x in stride(from: 0, through: width, by: 2) {
        path.addLine(to: CGPoint(x: Double(x), y: ridge(Double(x), layer)))
    }
    path.addLine(to: CGPoint(x: width, y: 240)); path.closeSubpath()
    context.saveGState(); context.addPath(path); context.clip()
    let tops: [UInt32] = [0x40516d, 0x606681, 0x80798f, 0xa38b9f]
    gradient([color(tops[layer]), color(0x30465f)], from: CGPoint(x: 0, y: 580), to: CGPoint(x: 0, y: 260))
    // Irregular rock facets and short strata. No repeated full-height stripes.
    for _ in 0..<2500 {
        let x = random() * 1600
        let summit = ridge(x, layer)
        let y = 270 + random() * max(1, summit - 270)
        let length = 3 + random() * 35
        let direction = sin(x * 0.015 + Double(layer))
        context.setStrokeColor(color(direction > 0 ? 0xe9b1ac : 0x142e49, CGFloat(0.025 + random() * 0.10)))
        context.setLineWidth(CGFloat(0.5 + random() * 3))
        context.move(to: CGPoint(x: x, y: y))
        context.addLine(to: CGPoint(x: x + direction * length * 0.6, y: y - length))
        context.strokePath()
    }
    context.restoreGState()
}
context.saveGState(); context.clip(to: CGRect(x: 0, y: 0, width: width, height: 320))
gradient([color(0xb493a6), color(0x627e96), color(0x1b465b)], from: CGPoint(x: 0, y: 320), to: .zero)
for y in stride(from: 15, to: 315, by: 3) {
    let spread = Double(320 - y) * 1.5
    context.setStrokeColor(color(0xf8c5bb, CGFloat(0.02 + 0.08 * abs(sin(Double(y))))))
    context.setLineWidth(1)
    context.move(to: CGPoint(x: 800 - spread, y: Double(y)))
    context.addLine(to: CGPoint(x: 1000 + spread * sin(Double(y)), y: Double(y)))
    context.strokePath()
}
context.restoreGState()
for side in 0...1 {
    let path = CGMutablePath()
    func point(_ x: Double, _ y: Double) -> CGPoint { CGPoint(x: side == 0 ? x : 1600 - x, y: y) }
    let shift = side == 0 ? 0.0 : -45.0
    path.move(to: point(0, 420 + shift)); path.addLine(to: point(78, 358 + shift)); path.addLine(to: point(130, 330 + shift))
    path.addLine(to: point(160, 315 + shift)); path.addLine(to: point(212, 303 + shift)); path.addLine(to: point(240, 296 + shift))
    path.addLine(to: point(285, 267 + shift)); path.addLine(to: point(330, 255 + shift)); path.addLine(to: point(370, 220 + shift))
    path.addLine(to: point(290, 211 + shift)); path.addLine(to: point(250, 202 + shift)); path.addLine(to: point(220, 185 + shift))
    path.addLine(to: point(270, 150)); path.addLine(to: point(340, 110)); path.addLine(to: point(440, 0))
    path.addLine(to: point(0, 0)); path.closeSubpath()
    context.saveGState(); context.addPath(path); context.clip()
    gradient([color(0x32485b), color(0x112c3d)], from: CGPoint(x: 0, y: 420), to: .zero)
    context.restoreGState()
}
let image = context.makeImage()!
let destination = URL(fileURLWithPath: "core/ui/src/main/res/drawable-nodpi")
try FileManager.default.createDirectory(at: destination, withIntermediateDirectories: true)
func write(_ image: CGImage, _ name: String) throws {
    let bitmap = NSBitmapImageRep(cgImage: image)
    try bitmap.representation(using: .png, properties: [:])!.write(to: destination.appendingPathComponent(name))
}
try write(image, "glass_landscape.png")
let input = CIImage(cgImage: image)
let filter = CIFilter(name: "CIGaussianBlur")!
filter.setValue(input.clampedToExtent(), forKey: kCIInputImageKey)
filter.setValue(25, forKey: kCIInputRadiusKey)
let blurred = CIContext().createCGImage(filter.outputImage!.cropped(to: input.extent), from: input.extent)!
try write(blurred, "glass_landscape_frost.png")
