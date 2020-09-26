import { api as entityModule } from './entity-module/GraphicEntityModule.js'
import { EntityFactory } from './entity-module/EntityFactory.js'

export class BoardModule {
    constructor (assets) {
        this.previousFrame = {}
        this.moveCache = []
        this.moveTurnCache = []
        this.SIZE = 4
        this.runtimeId = 1000000

        this.grid = new Array(this.SIZE)
        for (var x = 0; x < this.SIZE; x++) {
            this.grid[x] = new Array(this.SIZE)
            for (var y = 0; y < this.SIZE; y++) {
                var text = EntityFactory.create("T")
                text.id = this.runtimeId++
                entityModule.entities.set(text.id, text)
                var rect = EntityFactory.create("R")
                rect.id = this.runtimeId++
                entityModule.entities.set(rect.id, rect)
                this.grid[x][y] = {"x":x, "y":y, "value":0, "text":text, "rect":rect}
            }
        }

        BoardModule.refreshContent = () => {}
    }

    static refreshContent () {}

    static get name () {
        return 'x'
    }

    updateScene (previousData, currentData, progress) {
    }

    getBackgroundColor(value) {
        if (value == 2) return 0xeee4da
        if (value == 4) return 0xede0c8
        if (value == 8) return 0xf2b179
        if (value == 16) return 0xf59563
        if (value == 32) return 0xf67c5f
        if (value == 64) return 0xf65e3b
        if (value == 128) return 0xedcf72
        if (value == 256) return 0xedcc61
        if (value == 512) return 0xedc850
        if (value == 1024) return 0xedc53f
        if (value == 2048) return 0xedc22e
        return 0x3c3a32
    }

    getTextColor(value) {
        if (value == 2) return 0x776e65
        if (value == 4) return 0x776e65
        return 0xf9f6f2
    }

    placeEntity(text, rect, cell, time, value, visible, frameInfo) {
        text.addState(time, {values: { ...text.defaultState,
                                      fillColor:this.getTextColor(value),
                                      fontSize:100,
                                      x:210*cell.x+530,
                                      y:210*cell.y+170,
                                      visible:visible,
                                      textAlign:"center",
                                      text:""+value,
                                      zIndex:1,
                                      t:time},
                            curve:{}}, frameInfo.number, frameInfo)

        rect.addState(time, {values: { ...rect.defaultState,
                                      fillColor:this.getBackgroundColor(value),
                                      fontSize:100,
                                      x:210*cell.x+440,
                                      y:210*cell.y+130,
                                      visible:visible,
                                      width:190,
                                      height:190,
                                      t:time},
                            curve:{}}, frameInfo.number, frameInfo)
    }

    animateMove(from, to, tEnd, value, frameInfo) {
        if (this.moveCache.length == 0) {
            var text = EntityFactory.create("T")
            text.id = this.runtimeId++
            entityModule.entities.set(text.id, text)
            var rect = EntityFactory.create("R")
            rect.id = this.runtimeId++
            entityModule.entities.set(rect.id, rect)
            var anim = {"text":text, "rect":rect}
        } else {
            var anim = this.moveCache.pop()
        }

        this.placeEntity(anim.text, anim.rect, from, 0, value, true, frameInfo)
        this.placeEntity(anim.text, anim.rect, to, tEnd, value, false, frameInfo)
        this.moveTurnCache.push(anim)
    }



    updateValue(cell, time, frameInfo) {
        this.placeEntity(cell.text, cell.rect, cell, time, cell.value, cell.value > 0, frameInfo)
    }

    applyMove(frameInfo, dir) {
        var turnScore = 0
        var merged = new Array(this.SIZE)
        for (var i = 0; i < this.SIZE; i++) merged[i] = new Array(this.SIZE)
        var targetStart = [0, this.SIZE - 1, this.SIZE * (this.SIZE - 1), 0][dir]
        var targetStep = [1, this.SIZE, 1, this.SIZE][dir]
        var sourceStep = [this.SIZE, -1, -this.SIZE, 1][dir]
        var tStep = 0.2

        for (var i = 0; i < this.SIZE; i++) {
            var finalTarget = targetStart + i * targetStep
            for (var j = 1; j < this.SIZE; j++) {
                var source = finalTarget + j * sourceStep
                var sourceX = source % this.SIZE
                var sourceY = Math.floor(source / this.SIZE)
                var tEnd = 0
                var finalCell = this.grid[sourceX][sourceY]
                var initialCell = finalCell
                var value = finalCell.value
                if (this.grid[sourceX][sourceY].value == 0) continue
                for (var k = j - 1; k >= 0; k--) {
                    var intermediate = finalTarget + k * sourceStep

                    var intermediateX = intermediate % this.SIZE
                    var intermediateY = Math.floor(intermediate / this.SIZE)
                    if (this.grid[intermediateX][intermediateY].value == 0) {
                        this.grid[intermediateX][intermediateY].value = this.grid[sourceX][sourceY].value
                        this.grid[sourceX][sourceY].value = 0
                        finalCell = this.grid[intermediateX][intermediateY]
                        source = intermediate
                        sourceX = source % this.SIZE
                        sourceY = Math.floor(source / this.SIZE)
                    } else {
                        if (!merged[intermediateX][intermediateY] && this.grid[intermediateX][intermediateY].value == this.grid[sourceX][sourceY].value) {
                            this.grid[sourceX][sourceY].value = 0
                            this.grid[intermediateX][intermediateY].value *= 2
                            finalCell = this.grid[intermediateX][intermediateY]
                            merged[intermediateX][intermediateY] = true
                            turnScore += this.grid[intermediateX][intermediateY].value
                            tEnd += tStep
                        }
                        break
                    }
                    tEnd += tStep
                }
                if (finalCell != initialCell) {
                    this.updateValue(initialCell, 0, frameInfo)
                    this.updateValue(finalCell, tEnd, frameInfo)
                    this.animateMove(initialCell, finalCell, tEnd, value, frameInfo)
                }
            }
        }

        return turnScore;
    }

    applySpawn(frameInfo, c) {
        var value = c.charCodeAt(0) >= 'a'.charCodeAt(0) ? 2 : 4
        var index = value == 2 ? (c.charCodeAt(0)-'a'.charCodeAt(0)) : (c.charCodeAt(0)-'A'.charCodeAt(0))
        var x = index % this.SIZE
        var y = Math.floor(index / this.SIZE)
        this.grid[x][y].value = value
        this.placeEntity(this.grid[x][y].text, this.grid[x][y].rect, this.grid[x][y], 1, this.grid[x][y].value, true, frameInfo)
    }

    handleFrameData (frameInfo, data) {
        if (frameInfo.number == 0) {
            var background = EntityFactory.create("S")
            background.id = this.runtimeId++
            entityModule.entities.set(background.id, background)
            background.addState(0, {values: { ...background.defaultState, image:"background.png", visible:true, zIndex:-1 }, curve:{}}, frameInfo.number, frameInfo)
        }
        if (!data) {
            return
        }
        var newRegistration = {}
        for (var i = 0; i < data.length; i++) {
            var c = data[i]
            var moveIndex = "^>v<".indexOf(c)
            if (moveIndex >= 0) this.applyMove(frameInfo, moveIndex)
            else this.applySpawn(frameInfo, c)
        }
        this.moveCache.push(...this.moveTurnCache)
        this.moveTurnCache = []
        const registered = { ...this.previousFrame.registered, ...newRegistration }
        const frame = { registered, number: frameInfo.number }
        this.previousFrame = frame
        return frame
    }

    reinitScene (container, canvasData) {
        BoardModule.refreshContent()
    }
}