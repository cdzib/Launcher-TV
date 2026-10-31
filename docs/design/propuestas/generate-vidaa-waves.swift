// Generative wallpaper inspired by the VIDAA hero: magenta/violet ribbons converging
// on the right over a deep purple sky that fades to near black behind the app grid.
// Run: swift docs/design/propuestas/generate-vidaa-waves.swift <output.png>
import AppKit

let width = 1920, height = 1080
let space = CGColorSpaceCreateDeviceRGB()
let context = CGContext(data: nil, width: width, height: height, bitsPerComponent: 8,
    bytesPerRow: width * 4, space: space, bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue)!

func color(_ hex: UInt32, _ alpha: CGFloat = 1) -> CGColor {
    CGColor(colorSpace: space, components: [
        CGFloat((hex >> 16) & 0xFF) / 255,
        CGFloat((hex >> 8) & 0xFF) / 255,
        CGFloat(hex & 0xFF) / 255,
        alpha
    ])!
}

let w = CGFloat(width), h = CGFloat(height)
// SVG-like top-down coordinates.
context.translateBy(x: 0, y: h)
context.scaleBy(x: 1, y: -1)

// ── Sky ────────────────────────────────────────────────────────────────────────
context.setFillColor(color(0x0B0B0E))
context.fill(CGRect(x: 0, y: 0, width: w, height: h))
let sky = CGGradient(colorsSpace: space, colors: [
    color(0x2B0A3F), color(0x45105F), color(0x3A0E52), color(0x1B0B28), color(0x0E0E12)
] as CFArray, locations: [0, 0.18, 0.36, 0.52, 0.68])!
context.drawLinearGradient(sky, start: CGPoint(x: 0, y: 0), end: CGPoint(x: 0, y: h * 0.72), options: [.drawsAfterEndLocation])

// ── Soft glows ────────────────────────────────────────────────────────────────
func glow(_ center: CGPoint, _ radius: CGFloat, _ tint: CGColor, _ alpha: CGFloat) {
    context.saveGState()
    context.setBlendMode(.plusLighter)
    let stop = tint.copy(alpha: alpha)!
    let clear = tint.copy(alpha: 0)!
    let gradient = CGGradient(colorsSpace: space, colors: [stop, clear] as CFArray, locations: [0, 1])!
    context.drawRadialGradient(gradient, startCenter: center, startRadius: 0,
        endCenter: center, endRadius: radius, options: [])
    context.restoreGState()
}

glow(CGPoint(x: w * 0.86, y: h * 0.12), h * 0.34, color(0xFF2D95), 0.55)
glow(CGPoint(x: w * 0.98, y: h * 0.30), h * 0.26, color(0xB04CFF), 0.45)
glow(CGPoint(x: w * 0.55, y: h * 0.06), h * 0.30, color(0x7B2CFF), 0.30)
glow(CGPoint(x: w * 0.08, y: h * 0.44), h * 0.30, color(0x8A3BD0), 0.34)
glow(CGPoint(x: w * 0.22, y: h * 0.52), h * 0.16, color(0xC56BFF), 0.20)

// ── Ribbons ───────────────────────────────────────────────────────────────────
func ribbon(startY: CGFloat, targetY: CGFloat, spread: CGFloat, phase: CGFloat,
            frequency: CGFloat, thickness: CGFloat, from: CGColor, to: CGColor, alpha: CGFloat) {
    let steps = 240
    var centers: [CGPoint] = []
    for step in 0...steps {
        let t = CGFloat(step) / CGFloat(steps)
        let x = -420 + t * (w + 560)
        let ease = pow(t, 1.55)
        let wave = sin(t * frequency * 2 * .pi + phase) * spread * (1 - t * 0.55)
        centers.append(CGPoint(x: x, y: startY + (targetY - startY) * ease + wave))
    }
    func halfWidth(_ t: CGFloat) -> CGFloat { thickness * (1 - 0.62 * t) / 2 }
    let band = CGMutablePath()
    band.move(to: CGPoint(x: centers[0].x, y: centers[0].y - halfWidth(0)))
    for (index, point) in centers.enumerated().dropFirst() {
        band.addLine(to: CGPoint(x: point.x, y: point.y - halfWidth(CGFloat(index) / CGFloat(steps))))
    }
    for (index, point) in centers.enumerated().reversed() {
        band.addLine(to: CGPoint(x: point.x, y: point.y + halfWidth(CGFloat(index) / CGFloat(steps))))
    }
    band.closeSubpath()

    context.saveGState()
    context.addPath(band)
    context.clip()
    let gradient = CGGradient(colorsSpace: space, colors: [
        from.copy(alpha: alpha)!, to.copy(alpha: alpha)!
    ] as CFArray, locations: [0, 1])!
    context.setBlendMode(.plusLighter)
    context.drawLinearGradient(gradient, start: CGPoint(x: 0, y: startY),
        end: CGPoint(x: w, y: targetY), options: [])
    context.restoreGState()
}

// Wide atmospheric bands first, then bright cores on top.
let palettes: [(UInt32, UInt32)] = [
    (0x6E1FA8, 0xFF3D8B), (0xFF4FA3, 0x7B3BFF), (0x3B4FE0, 0xB44CFF),
    (0xFF2D95, 0x38D6F0), (0x8B5CF6, 0xFF6FB5), (0x2C1B6B, 0xE04CFF),
    (0xFF7ABF, 0x5B6BFF), (0x9B2BD6, 0x3AC8FF)
]
for (index, palette) in palettes.enumerated() {
    let start = CGFloat(index) * 118 - 90
    let target = CGFloat(190 + (index % 3) * 70)
    let spread = CGFloat(30 + (index % 4) * 18)
    let frequency = CGFloat(0.7 + CGFloat(index % 5) * 0.22)
    let phase = CGFloat(index) * 0.9
    let thickness = CGFloat(70 + (index % 3) * 46)
    ribbon(startY: start, targetY: target, spread: spread, phase: phase, frequency: frequency,
        thickness: thickness, from: color(palette.0), to: color(palette.1), alpha: 0.42)
    ribbon(startY: start, targetY: target, spread: spread, phase: phase, frequency: frequency,
        thickness: thickness * 0.32, from: color(palette.0), to: color(0xFFFFFF), alpha: 0.14)
}

// ── Bottom fade so app tiles keep contrast ────────────────────────────────────
let fade = CGGradient(colorsSpace: space, colors: [
    color(0x0C0C0F, 0), color(0x0C0C0F, 0.55), color(0x0C0C0F, 1)
] as CFArray, locations: [0, 0.5, 1])!
context.drawLinearGradient(fade, start: CGPoint(x: 0, y: h * 0.22), end: CGPoint(x: 0, y: h * 0.58), options: [.drawsAfterEndLocation])

// Vignette on the corners to avoid flat edges.
glow(CGPoint(x: w / 2, y: h * 1.02), h * 0.42, color(0x000000), 0.55)

let image = NSBitmapImageRep(cgImage: context.makeImage()!)
try image.representation(using: .png, properties: [:])!.write(to: URL(fileURLWithPath: CommandLine.arguments[1]))
