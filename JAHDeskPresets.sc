JAHDeskPresets{
	var <>presetDict;
	var <>presetMenu;
	var <>presetText;
	var <>saveButt,<>loadButt,<>removeButt;
	var tempName;
	var presetSelection;
	var parentView;
	var <>desk;
	*new{|parent|
		^super.new.initJAHDeskPresets(parent);
	}
	
	initJAHDeskPresets{|argparent|
		parentView = argparent.dashboardView;
		desk = argparent;
		if(File.exists("JAH/deskPresets/"++desk.name.asString)){
			presetDict = Object.readArchive("JAH/deskPresets/"++desk.name.asString);
		}{presetDict = ();};
		presetMenu = ListView(parentView,Rect(0,0,160,200))
					.canFocus_(false)
					.font_(Font("Helvetica",9))
					.items_(presetDict.keys.asArray.flat)
					.action_({|item|
						presetSelection = item.item;
						presetText.string = item.item;
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
		removeButt = RoundButton(parentView,Rect(0,0,80,20))
					.canFocus_(false)
					.extrude_(false)
					.border_(1)
					.font_(Font("Helvetica",9))
					.states_([["Delete Preset",Color.black,Color.white]])
					.action_({|butt|
						this.removePreset(presetMenu.item);
					});
	}

	removePreset{|preset|
		preset = preset.asSymbol;
		presetDict.removeAt(preset);
		presetDict.writeArchive("JAH/deskPresets/"++desk.name.asString);
		presetMenu.items_(presetDict.keys.asArray.flat);
	}
	loadPreset{|preset|
		var trackArray;
		preset = preset.asSymbol;
		trackArray = presetDict.at(preset);
		desk.tracks.do{|track,i|
			track[1][0].insertList.do{|list,j|
				list.listView.valueAction_(trackArray[i][j][0]);
				if(list.fx.notNil){
					list.fx.loadPreset(trackArray[i][j][1]);
				}
				}
			}
	}

	savePreset{|preset|
		var trackArray = [];
		var fxPreset;
		var insertArray = [];
		preset.postln;
		desk.tracks.do{|track,i|
			insertArray = [];
			track[1][0].insertList.do{|list,i|
				if(list.fx.notNil){
					fxPreset = list.fx.currentPreset;
				}{fxPreset = \NONE;};
				insertArray = insertArray.add([list.listView.value,fxPreset]);
				};
			trackArray = trackArray.add(insertArray);
			track.postln;
			};
		presetDict.put(preset.asSymbol,trackArray);
		presetDict.writeArchive("JAH/deskPresets/"++desk.name.asString);
		presetMenu.items_(presetDict.keys.asArray.flat);
	}
}