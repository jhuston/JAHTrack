JAHfxGui{
	var name, params,settings;
	var paramKeys;
	var <paramList,<>paramDict,<>settingsDict;
	var <>win,knobSheet;
	var <>knobList;
	var bounds;
	var width,height;
	var <>outbus,<>inbus;
	var onBtn;
	var outputs,inputs;
	var midiBtn;
	var <>fx;
	var <itemBase;
	var <>presetList,<>presetText,<>savePresetButt,<>removePresetButt;
	
	*new{|name, params|
		^super.new.initJAHfxGui(name,params);
	}

/*settingsDict = key:[label,initial value,gui control type (\knob,\slider etc...),spec]	*/
//	
	initJAHfxGui{|argParams|
		itemBase = [\NONE,"-"];
		settingsDict = argParams ?? ();
		knobList = List.new;
		paramDict = ();
		paramKeys = settingsDict.at(\params);
		name = settingsDict.at(\synthSelector).asString ? "Generic JAH GUI";
/*		this.initGUI();*/
		"init Method JAHfxGui".postln;
/*		this.initMIDI();*/
	}
	
	initGUI{
		width = paramKeys.size * 95;
		height = 120;
		win = SCWindow(name,Rect(100,100,width,height)).front;
		win.userCanClose_(false);
		bounds = Rect(10,10,width,height);
		knobSheet = FlowView(win);
		presetList = PopUpMenu(knobSheet,Rect(0,0,80,15))
				.font_(Font("Helvetica",9))
				.items_(itemBase++fx.presetsDict.keys.asArray.flat.sort)
				.action_({|menu|
						if(menu.item != \NONE){
							fx.loadPreset(menu.item);
						}
					});
		presetList.value_(presetList.items.indexOf(fx.currentPreset));
		presetText = TextField(knobSheet,Rect(0,0,60,15))
				.font_(Font("Helvetica",9));
		savePresetButt = RoundButton(knobSheet,Rect(0,0,40,15))
				.font_(Font("Helvetica",9))
				.states_([["save",Color.black,Color.white]])
				.border_(1)
				.canFocus_(false)
				.extrude_(false)
				.action_({|butt|
					fx.savePreset(presetText.string);
					});
		removePresetButt = RoundButton(knobSheet,Rect(0,0,40,15))
				.font_(Font("Helvetica",9))
				.states_([["delete",Color.black,Color.white]])
				.border_(1)
				.canFocus_(false)
				.extrude_(false)
				.action_({|butt|
					fx.removePreset(presetList.item);
					});
		knobSheet.startRow;
		paramKeys.do{|param,i|
			var rowCount = i%3;
			paramDict.put(param,fx.fxParams.at(param).val);
			if(rowCount>=3){knobSheet.startRow;};
			fx.fxParams.at(param).control_(
	EZKnob(knobSheet,90@40,label:fx.fxParams.at(param).label,initVal:fx.fxParams.at(param).val,controlSpec:fx.fxParams.at(param).spec,unitWidth:0,layout:\line2,margin:2@2)
					.font_(Font("Helvetica",10))
					.action_({|knob| knob.value.postln;
						fx.fxParams.at(param).val_(knob.value);
						try{fx.synth.set(param,knob.value)};
						});
				);
			};
	
	inputs = NumberBox(knobSheet,20@20)
			.action_({|val|
				inbus = val.value;
				fx.synth.set(\inbus,inbus);
				});
	outputs = NumberBox(knobSheet,20@20)
			.action_({|val| 
				outbus = val.value;
				fx.synth.set(\outbus,outbus);
				});
	onBtn = RoundButton(knobSheet,20@20)
			.states_([[\power,Color.black,Color.white],[\power,Color.red,Color.white]])
			.mouseDownAction_({|btn|
				btn.value.postln;
				if(btn.value == 0){
					fx.on;
					}
				{fx.off;};
				});
	midiBtn = RoundButton(knobSheet,20@20)
				.states_([["M",Color.black,Color.white],["M",Color.red,Color.white]])
				.mouseDownAction_({|btn|
					btn.value.postln;
					if(btn.value == 0){
						this.initMIDI();
						}
					{this.clearMIDI()};
					});
	}
	
	setParam{|param,val|
		{paramDict.at(param).control.valueAction_(val)}.defer(0.001);
		}
	
	initMIDI{
		paramDict.do{|item|
			if(item.midiType.notNil){
				if(item.midiType[0]===\cc){
					item.midiCntrl = CCResponder({|src,chan,num,value|this.setParam(item.symbol,item.spec.map(value/127));},nil,nil,item.midiType[1],nil);
				};
			};
		}
	}
	
	clearMIDI{
		paramDict.do{|item|
		item.midiCntrl.remove;
		}
	}
	cleanUp{
		try{this.clearMIDI();};
		try{fx.synth.free};
	}
	
	
}