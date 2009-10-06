//JAHfxParams: container class to hold parameters for fx gui and midi control.
JAHfxParams{
	var <>param,<>val,<>label,<>spec,<>midiCntrl,<>valAction,<>midiType,<>guiControl;
	
	*new{
		^super.new.initJAHfxParams();
	}
	
	initJAHfxParams{

		}
	
	relateControl_{|argGuiControl|
		guiControl = argGuiControl;
		}
}


JAHfxGui{
	var name, params,settings;
	var paramKeys;
	var <paramList,<>paramDict,<>settingsDict;
	var <>win,knobSheet;
	var knobList;
	var bounds;
	var width,height;
	var <>outbus,<>inbus;
	var onBtn;
	var outputs,inputs;
	var midiBtn;
	var <>fx;
	
	*new{|name, params|
		^super.new.initJAHfxGui(name,params);
	}

/*settingsDict = key:[label,initial value,gui control type (\knob,\slider etc...),spec]	*/
//	
	initJAHfxGui{|argParams|
		settingsDict = argParams ?? ();
		knobList = List.new;
		paramDict = ();
		paramKeys = settingsDict.at(\params);
		name = settingsDict.at(\synthSelector).asString ? "Generic JAH GUI";
		this.initGUI();
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
		paramKeys.do{|param,i|
			var rowCount = i%3;
			paramDict.put(param,JAHfxParams.new);
			if(rowCount>=3){knobSheet.startRow;};
			paramDict.at(param).relateControl_(
				EZKnob(knobSheet,90@40,label:settingsDict.label[i],initVal:settingsDict.val[i],controlSpec:settingsDict.spec[i],unitWidth:0,layout:\line2,margin:2@2)
					.font_(Font("Helvetica",10))
					.action_({|knob| knob.value.postln;
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
		{paramDict.at(param).guiControl.valueAction_(val)}.defer(0.001);
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