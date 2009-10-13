JAHDeskPresets{
	var <>presetDict;
	var <>presetMenu;
	var <>presetText;
	var <>saveButt,<>loadButt;
	var tempName;
	var presetSelection;
	var parentView;
	*new{|parent|
		^super.new.initJAHDeskPresets(parent);
	}
	
	initJAHDeskPresets{|argparent|
		parentView = argparent.dashboardView;
		if(File.exists("JAH/deskPresets/deskPresets")){
			presetDict = Object.readArchive("JAH/deskPresets/deskPresets");
		}{presetDict = ();};
		presetMenu = ListView(parentView,Rect(0,0,160,200))
					.canFocus_(false)
					.font_(Font("Helvetica",9))
					.items_(presetDict.keys.asArray.flat)
					.action_({|item|
						presetSelection = item.item;
					});
		presetText = TextField(parentView,Rect(0,0,160,15))
					.font_(Font("Helvetica",9))
					.keyUpAction_({|field|
						tempName = field.value;
					});
		saveButt = RoundButton(parentView,Rect(0,0,80,20))
					.canFocus_(false)
					.extrude_(false)
					.border_(1)
					.font_(Font("Helvetica",9))
					.states_([["Save Preset",Color.black,Color.white]])
					.action_({|butt|
						this.savePreset(tempName);
					});
		loadButt = RoundButton(parentView,Rect(0,0,80,20))
					.canFocus_(false)
					.extrude_(false)
					.border_(1)
					.font_(Font("Helvetica",9))
					.states_([["Load Preset",Color.black,Color.white]])
					.action_({|butt|
						this.loadPreset(presetMenu.item);
					});
	}

	loadPreset{|preset|
		preset.postln;
	}

	savePreset{|preset|
		preset.postln;
		presetDict.put(preset.asSymbol,"test");
		presetDict.writeArchive("JAH/deskPresets/deskPresets");
		presetMenu.items_(presetDict.keys.asArray.flat);
	}
}