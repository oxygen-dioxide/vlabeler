let parameters = params["parameters"]
let source = params["source"]
let target = params["target"]
let ratio = source/target

if (debug) {
    console.log(`Original tempo: ${source}bpm`)
    console.log(`New tempo: ${target}bpm`)
    console.log(`Parameters to scale: ${parameters}`)
}

for (let entry of entries) {
    let offset = entry.points[3]
    let fixed = entry.points[0] - offset
    let preutterance = entry.points[1] - offset
    let overlap = entry.points[2] - offset
    let cutoff = entry.end
    if (cutoff > 0) {
        cutoff = -(cutoff - offset)
    } else {
        cutoff = -cutoff
    }

    let newOffset = offset * ratio
    entry.start = entry.start * ratio
    entry.points[3] = newOffset
    
    if (parameters === "all") {
        entry.points[0] = newOffset + (fixed * ratio)
        entry.points[1] = newOffset + (preutterance * ratio)
        entry.points[2] = newOffset + (overlap * ratio)

        let newCutoff = cutoff * ratio
        if (newCutoff < 0){
            entry.end = -newCutoff + newOffset
        } else {
            entry.end = -newCutoff
        }
    } else if (parameters === "offset") {
        entry.points[0] = newOffset + fixed
        entry.points[1] = newOffset + preutterance
        entry.points[2] = newOffset + overlap

        if (cutoff < 0){
            entry.end = -cutoff + newOffset
        } else {
            entry.end = -cutoff
        }
    }
}
