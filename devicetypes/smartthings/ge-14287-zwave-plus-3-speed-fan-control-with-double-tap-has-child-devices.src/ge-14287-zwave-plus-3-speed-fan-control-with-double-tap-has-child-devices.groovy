/**
 *  GE 14287 Z-Wave Plus 3-Speed Fan Control with Double Tap - Has Child Devices
 *
 *  A better functional Device Type for Z-Wave Smart Fan Control Switches + Double Tap
 *  Particularly the GE 14287 Z-Wave Smart Fan Control.
 *
 *  Copyright 2018 STDevMan
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Contributors:
 *  STDevMan (Author): Combined Z-wave 3 Speed Fan Code + Double Tap etc
 *  Chris Nussbaum (nuttytree): GE/Jasco Z-Wave Plus Dimmer Switch
 *  Jonathan Hamstead (jhamstead): Z-Wave Fan Control
 *  ChadK: Original 3 Speed Switch Code
 *  Child Device Information From KOF Zigbee Fan Controller:
 *     Ranga Pedamallu; Stephan Hackett; Dale Coffing
 *
 *
 *  DO NOT USE THIS VERSION IF YOU NEED TO ADD THE DEVICE TO A SCENE
 *
 */
metadata {
	definition (name: "GE 14287 Z-Wave Plus 3-Speed Fan Control with Double Tap - Has Child Devices", namespace: "smartthings", author: "STDevMan") {
		capability "Switch Level"
		capability "Button"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"

		command "doubleUp"
        command "doubleDown"
		command "lowSpeed"
		command "medSpeed"
		command "highSpeed"
		command "inverted"
        command "notInverted"

		attribute "currentState", "string"
		attribute "inverted", "enum", ["inverted", "not inverted"]
	}

	tiles (scale:2) {
		multiAttributeTile(name: "switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.currentState", key: "PRIMARY_CONTROL") {
				attributeState "default", label:'ADJUSTING', action:"refresh.refresh", icon:"st.Lighting.light24", backgroundColor:"#30D0FF", nextState: "turningOff"
				attributeState "HIGH", label:'HIGH', action:"switch.off", icon:"st.Lighting.light24", backgroundColor:"#00A0DC", nextState: "turningOff"
				attributeState "MED", label:'MED', action:"switch.off", icon:"st.Lighting.light24", backgroundColor:"#00A0DC", nextState: "turningOff"
				attributeState "LOW", label:'LOW', action:"switch.off", icon:"st.Lighting.light24", backgroundColor:"#00A0DC", nextState: "turningOff"
				attributeState "OFF", label:'OFF', action:"switch.on", icon:"st.Lighting.light24", backgroundColor:"#ffffff", nextState: "turningOn"
				attributeState "turningOn", action:"switch.on", label:'TURNING ON', icon:"st.Lighting.light24", backgroundColor:"#30D0FF", nextState: "turningOn"
				attributeState "turningOff", action:"switch.off", label:'TURNING OFF', icon:"st.Lighting.light24", backgroundColor:"#30D0FF", nextState: "turningOff"
			}
			tileAttribute ("device.level", key: "SECONDARY_CONTROL") {
				attributeState "level", label:'${currentValue}%'
			}
		}

        childDeviceTile("fanMode1", "fanMode1", height: 2, width: 2)
        childDeviceTile("fanMode2", "fanMode2", height: 2, width: 2)
        childDeviceTile("fanMode3", "fanMode3", height: 2, width: 2)
		
		standardTile("doubleUp", "device.button", width: 3, height: 2, decoration: "flat") {
			state "default", label: "Tap ▲▲", backgroundColor: "#ffffff", action: "doubleUp", icon: "https://raw.githubusercontent.com/STDevMan/SmartThings-STDevMan/master/icons/SwitchOnIcon.png"
		}     
 
        standardTile("doubleDown", "device.button", width: 3, height: 2, decoration: "flat") {
			state "default", label: "Tap ▼▼", backgroundColor: "#ffffff", action: "doubleDown", icon: "https://raw.githubusercontent.com/STDevMan/SmartThings-STDevMan/master/icons/SwitchOffIcon.png"
		}
		
		standardTile("inverted", "device.inverted", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
			state "not inverted", label: "Not Inverted", action:"inverted", icon:"https://raw.githubusercontent.com/STDevMan/SmartThings-STDevMan/master/icons/SwitchNotInverted.png", backgroundColor: "#ffffff"
			state "inverted", label: "Inverted", action:"notInverted", icon:"https://raw.githubusercontent.com/STDevMan/SmartThings-STDevMan/master/icons/SwitchInverted.png", backgroundColor: "#ffffff"
		}
		controlTile("levelSliderControl", "device.level", "slider", width: 2, height: 2, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
		
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		main(["switch"])
		details(["switch", "fanMode1", "fanMode2", "fanMode3", "doubleUp", "doubleDown", "inverted", "levelSliderControl", "refresh"])
	}
	preferences {
		section("Fan Thresholds") {
			input "lowThreshold", "number", title: "Low Threshold (typical is 1-33)", range: "1..99", defaultValue: 33
			input "medThreshold", "number", title: "Medium Threshold (typical is 34-67)", range: "1..99", defaultValue: 67
			input "highThreshold", "number", title: "High Threshold (typical is 68-99)", range: "1..99", defaultValue: 99
		}
		input (
            type: "paragraph",
            element: "paragraph",
            title: "Configure Association Groups:",
            description: "Devices in association group 2 will receive Basic Set commands directly from the switch when it is turned on or off. Use this to control another device as if it was connected to this switch.\n\n" +
                         "Devices in association group 3 will receive Basic Set commands directly from the switch when it is double tapped up or down.\n\n" +
                         "Devices are entered as a comma delimited list of IDs in hexadecimal format."
        )

        input (
            name: "requestedGroup2",
            title: "Association Group 2 Members (Max of 5):",
            type: "text",
            required: false
        )

        input (
            name: "requestedGroup3",
            title: "Association Group 3 Members (Max of 4):",
            type: "text",
            required: false
        )
	}
}

def parse(String description) {
	def item1 = [
		canBeCurrentState: false,
		linkText: getLinkText(device),
		isStateChange: false,
		displayed: false,
		descriptionText: description,
		value:  description
	]
	def result
	def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
	if (cmd) {
		result = createEvent(cmd, item1)
	}
	else {
		item1.displayed = displayed(description, item1.isStateChange)
		result = [item1]
	}
	log.debug "Parse returned ${result?.descriptionText}"
	result
}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
  	result[i].type = "physical"
	}
	log.trace "BasicReport"
  result
}

def createEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd, Map item1) {
	log.debug "---BASIC SET V1--- ${device.displayName} sent ${cmd}"
	def result
	if (cmd.value == 255) {
    	result = createEvent([name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "Double-tap up (button 1) on $device.displayName", isStateChange: true, type: "physical"])
    }
	else if (cmd.value == 0) {
    	result = createEvent([name: "button", value: "pushed", data: [buttonNumber: 2], descriptionText: "Double-tap down (button 2) on $device.displayName", isStateChange: true, type: "physical"])
    }
	else{
	result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	log.trace "BasicSet"
	result
	}
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStartLevelChange cmd, Map item1) {
	[]
	log.trace "StartLevel"
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd, Map item1) {
	[response(zwave.basicV1.basicGet())]
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "physical"
	}
	log.trace "SwitchMultiLevelSet"
	result
}

def createEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd, Map item1) {
	def result = doCreateEvent(cmd, item1)
	result[0].descriptionText = "${item1.linkText} is ${item1.value}"
	result[0].handlerName = cmd.value ? "statusOn" : "statusOff"
	for (int i = 0; i < result.size(); i++) {
		result[i].type = "digital"
	}
	log.trace "SwitchMultilevelReport"
	result
}

def createEvent(physicalgraph.zwave.commands.crc16encapv1.Crc16Encap cmd, Map item1) {
	log.debug("zwaveEvent(): CRC-16 Encapsulation Command received: ${cmd}")
	def encapsulatedCommand = zwave.commandClass(cmd.commandClass)?.command(cmd.command)?.parse(cmd.data)
	if (!encapsulatedCommand) {
		log.debug("zwaveEvent(): Could not extract command from ${cmd}")
	} else {
		return zwaveEvent(encapsulatedCommand)
	}
}

def createEvent(physicalgraph.zwave.commands.associationv2.AssociationReport cmd, Map item1) {
	log.debug "---ASSOCIATION REPORT V2--- ${device.displayName} sent groupingIdentifier: ${cmd.groupingIdentifier} maxNodesSupported: ${cmd.maxNodesSupported} nodeId: ${cmd.nodeId} reportsToFollow: ${cmd.reportsToFollow}"
    if (cmd.groupingIdentifier == 3) {
    	if (cmd.nodeId.contains(zwaveHubNodeId)) {
        	createEvent(name: "numberOfButtons", value: 2, displayed: false)
        }
        else {
			sendHubCommand(new physicalgraph.device.HubAction(zwave.associationV2.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format()))
			sendHubCommand(new physicalgraph.device.HubAction(zwave.associationV2.associationGet(groupingIdentifier: 3).format()))
        	createEvent(name: "numberOfButtons", value: 0, displayed: false)
        }
    }
}

def createEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd, Map item1) {
    log.debug "---CONFIGURATION REPORT V2--- ${device.displayName} sent ${cmd}"
	def name = ""
    def value = ""
    def reportValue = cmd.configurationValue[0]
    switch (cmd.parameterNumber) {
        case 4:
            name = "inverted"
            value = reportValue == 1 ? "true" : "false"
            break
        default:
            break
    }
	createEvent([name: name, value: value, displayed: false])
}

def createEvent(physicalgraph.zwave.Command cmd,  Map map) {
	// Handles any Z-Wave commands we aren't interested in
	log.debug "UNHANDLED COMMAND $cmd"
}

def doCreateEvent(physicalgraph.zwave.Command cmd, Map item1) {
	def result = [item1]

	item1.name = "switch"
	
	item1.value = cmd.value ? "on" : "off"
	if (item1.value == "off") {
		sendEvent(name: "currentState", value: "OFF" as String)
        def children = getChildDevices()
        children.each {child->
           def childSpeedVal = child.getDataValue('speedVal')
           child.sendEvent(name:"fanSpeed", value:"default${childSpeedVal}")
        }
	}
	item1.handlerName = item1.value
	item1.descriptionText = "${item1.linkText} was turned ${item1.value}"
	item1.canBeCurrentState = true
	item1.isStateChange = isStateChange(device, item1.name, item1.value)
	item1.displayed = false

	if (cmd.value) {
		def item2 = new LinkedHashMap(item1)
		item2.name = "level"
		item2.value = cmd.value as String
		item2.unit = "%"
		item2.descriptionText = "${item1.linkText} dimmed ${item2.value} %"
		item2.canBeCurrentState = true
		item2.isStateChange = isStateChange(device, item2.name, item2.value)
		item2.displayed = false

        setTiles(item2.value.toInteger())
   
		result << item2
	}
	log.trace "doCreateEvent"
	result
}

def setTiles(value) {
	def lowThresholdvalue = (settings.lowThreshold != null && settings.lowThreshold != "") ? settings.lowThreshold.toInteger() : 33
	def medThresholdvalue = (settings.medThreshold != null && settings.medThreshold != "") ? settings.medThreshold.toInteger() : 67
	def highThresholdvalue = (settings.highThreshold != null && settings.highThreshold != "") ? settings.highThreshold.toInteger() : 99

    def children = getChildDevices()
    children.each {child->
        def childSpeedVal = child.getDataValue('speedVal')
	    if (value <= lowThresholdvalue && childSpeedVal == "01") {
            child.sendEvent(name:"fanSpeed", value:"on01")
            sendEvent(name:"currentState", value:"LOW")
        } else if (value >= lowThresholdvalue+1 && value <= medThresholdvalue && childSpeedVal == "02") {
            child.sendEvent(name:"fanSpeed", value:"on02")
            sendEvent(name:"currentState", value:"MED")
        } else if (value >= medThresholdvalue+1 && childSpeedVal == "03") {
            child.sendEvent(name:"fanSpeed", value:"on03")
            sendEvent(name:"currentState", value:"HIGH")
        } else {
            child.sendEvent(name:"fanSpeed", value:"default${childSpeedVal}")
        }
    }
}

def on() {
	log.info "on"
	delayBetween([zwave.basicV1.basicSet(value: 0xFF).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 1000)
}

def off() {
	log.info "off"
	delayBetween ([zwave.basicV1.basicSet(value: 0x00).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 1000)
}

def setLevel(value) {
	def level = Math.min(value as Integer, 99)

	log.trace "setLevel(value): ${level}"
    
    setTiles(level)

	delayBetween ([zwave.basicV1.basicSet(value: level as Integer).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 1000)
}

def setLevel(value, duration) {
	def level = Math.min(value as Integer, 99)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)

	log.trace "setLevel(value): ${level}"

    setTiles(level)
    
	zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format()
}

def poll() {
	def cmds = []
	cmds << zwave.switchMultilevelV1.switchMultilevelGet().format()
	cmds << zwave.switchBinaryV1.switchBinaryGet().format()
	delayBetween(cmds,500)
}

def ping() {
	refresh()
}

def refresh() {
	def cmds = []
	cmds << zwave.switchBinaryV1.switchBinaryGet().format()
	cmds << zwave.switchMultilevelV1.switchMultilevelGet().format()
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 3).format()
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 4).format()
    cmds << zwave.associationV2.associationGet(groupingIdentifier: 3).format()
	delayBetween(cmds,500)
}

def inverted() {
	sendEvent(name: "inverted", value: "inverted", display: false)
	zwave.configurationV2.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format()
}

def notInverted() {
	sendEvent(name: "inverted", value: "not inverted", display: false)
	zwave.configurationV2.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format()
}

def configure() {
    def cmds = []
    // Get current config parameter values
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 3).format()
    cmds << zwave.configurationV2.configurationGet(parameterNumber: 4).format()
    
    // Add the hub to association group 3 to get double-tap notifications
    cmds << zwave.associationV2.associationSet(groupingIdentifier: 3, nodeId: zwaveHubNodeId).format()
    cmds << zwave.associationV2.associationGet(groupingIdentifier: 3).format()
    delayBetween(cmds,500)
}

def updated() {
	if(state.oldLabel != device.label) {updateChildLabel()}
		initialize()
	if (state.lastUpdated && now() <= state.lastUpdated + 3000) return
    state.lastUpdated = now()

	def nodes = []
    def cmds = []

	if (settings.requestedGroup2 != state.currentGroup2) {
        nodes = parseAssocGroupList(settings.requestedGroup2, 2)
        cmds << zwave.associationV2.associationRemove(groupingIdentifier: 2, nodeId: [])
        cmds << zwave.associationV2.associationSet(groupingIdentifier: 2, nodeId: nodes)
        cmds << zwave.associationV2.associationGet(groupingIdentifier: 2)
        state.currentGroup2 = settings.requestedGroup2
    }

    if (settings.requestedGroup3 != state.currentGroup3) {
        nodes = parseAssocGroupList(settings.requestedGroup3, 3)
        cmds << zwave.associationV2.associationRemove(groupingIdentifier: 3, nodeId: [])
        cmds << zwave.associationV2.associationSet(groupingIdentifier: 3, nodeId: nodes)
        cmds << zwave.associationV2.associationGet(groupingIdentifier: 3)
        state.currentGroup3 = settings.requestedGroup3
    }
	sendHubCommand(cmds.collect{ new physicalgraph.device.HubAction(it.format()) }, 500)		
}

def installed() {
	initialize()
}

def initialize() {		
	log.info "Initializing"     
       	if(refreshChildren) {        	
            deleteChildren()            
    		device.updateSetting("refreshChildren", false)            
    	}
    	else {
			createFanChild()
            response(refresh())
    	}    	
}

def deleteChildren() {
	def children = getChildDevices()        	
    children.each {child->
  		deleteChildDevice(child.deviceNetworkId)
    }	
    log.info "Deleting children"                  
}

def updateChildLabel() {
	log.info "UPDATE LABEL"
	for(i in 1..3) {   		
    	def childDevice = getChildDevices()?.find {
        	it.device.deviceNetworkId == "${device.deviceNetworkId}-0${i}"
    	}                 
        if (childDevice) {childDevice.label = "${device.displayName} ${getFanName()["0${i}"]}"} // rename with new label
    }
    state.oldLabel = device.label //save the updated label for reference
}

def createFanChild() {
	state.oldLabel = device.label    //save the label for reference if it ever changes
	for(i in 1..3) {   		
    	def childDevice = getChildDevices()?.find {
        	it.device.deviceNetworkId == "${device.deviceNetworkId}-0${i}"
    	}                 
        if (!childDevice) {        
        	childDevice = addChildDevice("Z-Wave Fan Control - Child Device", "${device.deviceNetworkId}-0${i}", null,[completedSetup: true,
            label: "${device.displayName} ${getFanName()["0${i}"]}", isComponent: true, componentName: "fanMode${i}",
            componentLabel: "${getFanName()["0${i}"]}", "data":["speedVal":"0${i}"]])        	
           	log.info "Creating child fan mode ${childDevice}"
            def childSpeedVal = childDevice.getDataValue('speedVal')   
	        if (childSpeedVal) { childDevice.sendEvent(name:"fanSpeed", value:"default${childSpeedVal}") }
		}
       	else {
        	log.info "Child already exists"
		}
	}
}

def getFanName() { 
	[
    "01":"LOW",
    "02":"MEDIUM",
    "03":"HIGH"
	]
}

def lowSpeed() {
    def value = (settings.lowThreshold != null && settings.lowThreshold != "") ? settings.lowThreshold.toInteger() : 33
	setLevel(value)
}

def medSpeed() {
	def value = (settings.medThreshold != null && settings.medThreshold != "") ? settings.medThreshold.toInteger() : 67
	setLevel(value)
}

def highSpeed() {
	def value = (settings.highThreshold != null && settings.highThreshold != "") ? settings.highThreshold.toInteger() : 99
	setLevel(value)
}

def doubleUp() {
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: 1], descriptionText: "Double-tap up (button 1) on $device.displayName", isStateChange: true, type: "digital")
}

def doubleDown() {
	sendEvent(name: "button", value: "pushed", data: [buttonNumber: 2], descriptionText: "Double-tap down (button 2) on $device.displayName", isStateChange: true, type: "digital")
}

private parseAssocGroupList(list, group) {
    def nodes = group == 2 ? [] : [zwaveHubNodeId]
    if (list) {
        def nodeList = list.split(',')
        def max = group == 2 ? 5 : 4
        def count = 0

        nodeList.each { node ->
            node = node.trim()
            if ( count >= max) {
                log.warn "Association Group ${group}: Number of members is greater than ${max}! The following member was discarded: ${node}"
            }
            else if (node.matches("\\p{XDigit}+")) {
                def nodeId = Integer.parseInt(node,16)
                if (nodeId == zwaveHubNodeId) {
                	log.warn "Association Group ${group}: Adding the hub as an association is not allowed (it would break double-tap)."
                }
                else if ( (nodeId > 0) & (nodeId < 256) ) {
                    nodes << nodeId
                    count++
                }
                else {
                    log.warn "Association Group ${group}: Invalid member: ${node}"
                }
            }
            else {
                log.warn "Association Group ${group}: Invalid member: ${node}"
            }
        }
    }
    
    return nodes
}
