class Module {
    constructor(name, sampleDirectory, entries, currentIndex, rawFilePath, entryFilter = null, extras = {}) {
        this.name = name
        this.sampleDirectory = sampleDirectory // absolute path
        this.entries = entries
        this.currentIndex = currentIndex // current entry index
        this.rawFilePath = rawFilePath // absolute path to the raw label file
        this.extras = extras // extra information, defined in [LabelerConf.moduleExtraFields]
        // entryFilter: deprecated, no longer used
    }
}
