//TODO Adding things to the tracks array, and all classvar dicts not initializing properly. How do you only initialize on the first instantiation of a class?
// type is \input,\gen,\aux,\master
//params are [numberInserts,numChannels,outBus,inbus,numSends]
// defaults to [5,1,0]
JAHTrack{
	classvar <>typeDict,<>busDict,<>allTracks,<tracksinit = false;
	
	var <>track;
	var <>settingsDict;
	var <>currentPreset;
	var <>parentGroup,<trackGroup,<inputGroup,<insertGroup,<transGroup;
	var <>transOut,<>sendOut;
	var <>internalBus,<>outBus,<>inbus;
	var <>type,<>params;
	var <numChannels,<numInserts,<>numSends;
	var <>inputInsertList;
	var <>trackName,label;
	var s;
	var <>win,<>backgroundView,<>trackView,<>muteButt;
	var <transfer;
	var <>trackColor,<>typeColor;
	var <>desk,<>sendList,<>insertList;
	var <>input;
	
	*new{|parent,type,params|
			^super.new.initJAHTrack(parent,type,params);
		}
	
	*freeAll{
		allTracks.do{|track,i|
			track.trackGroup.free;
			track.internalBus.free;
			allTracks.remove(track).postln;
		}
	}
	*initClass{
		if(this.initialized.not){
		typeDict = ();
		busDict = ();
		allTracks = [];
	}
	}
	*initialized{
		^tracksinit;
	}
	
	initJAHTrack{|argparent,argType,argparams|
		desk = argparent;
		currentPreset = nil;
		tracksinit = true;
		//boot server if not running
		trackColor = Color.new255(124,24,200);
		typeColor = Color.new255(200,24,124);
		s = Server.default;
		params = argparams ? [nil,nil,nil,nil,nil];
		type = argType ? \input;
		numInserts = params[0] ? 4;
		numChannels = params[1] ? 1;
		outBus = params[2] ? 0;
		trackName = "JahTrack";
		internalBus = Bus.audio(s,numChannels);
		inbus = params[3] ?? 8;
		numSends = params[4] ?? 0;
		insertList = List.new;
		sendList = List.new;
		inputInsertList = List.new;
		transOut = 0;
//last step of initialization, add to tracks array;
		allTracks = allTracks.add(this);
	}
	
	buildGroup{|parent|
		parentGroup = parent ?? s;
		trackGroup = Group(parentGroup,\addToTail);
		inputGroup = Group(trackGroup,\addToHead);
		insertGroup = Group(inputGroup,\addAfter);
		transGroup = Group(trackGroup,\addToTail);
	}
	
	gui{|argparent,argPoint| //parent window for gui,topLeft Point for track View
		win = argparent;
		win.onClose_({JAHTrack.freeAll();});
		trackView = FlowView(win,Rect(argPoint.x,argPoint.y,125,400));
		label = SCStaticText(trackView,Rect(0,0,90,20))
				.font_(Font("Helvetica",12))
				.stringColor_(trackColor)
				.string_(trackName);
		muteButt = RoundButton(trackView,Rect(0,0,20,20))
				.canFocus_(false)
				.extrude_(false)
				.border_(1)
				.states_([[\speaker,Color.red,Color.white],[\speaker,Color.green,Color.white]])
				.action_({|butt|
					if(butt.value==1){
						transfer.powerOn();
					}{
						transfer.powerOff();
					};
					});
			SCStaticText(trackView,Rect(0,0,110,20))
				.font_(Font("Helvetica",10))
				.stringColor_(typeColor)
				.string_(type.asString);
		switch(type)
		{\input}{
			input = JAHTrackInput(this,inbus,internalBus,inputGroup);
		}
		{\gen}{
			input = JAHInsert(this,type).id_(type++1).insertGroup_(Group(inputGroup,\addToTail));
		}
		{\aux}{
			input = JAHInsert(this,type).id_(type++1).insertGroup_(Group(inputGroup,\addToTail));
		}
		{\master}{
			input = JAHInsert(this,type).id_(type++1).insertGroup_(Group(inputGroup,\addToTail));
		};
/*		input = JAHTrackInput(this,inbus,internalBus,inputGroup);
		
		inputInsertList.add(JAHInsert(this,type).id_(type++1).insertGroup_(Group(inputGroup,\addToTail)));*/
		if(numInserts>0){
		SCStaticText(trackView,Rect(0,0,110,20))
				.font_(Font("Helvetica",10))
				.stringColor_(trackColor)
				.string_("Effect Inserts");
		};
		numInserts.do{|i|
			insertList.add(JAHInsert(this,\insert).id_(\insert++i).insertGroup_(Group(insertGroup,\addToTail)));
		};
		
		if(numSends>0){
		numSends.do{|i|
			sendList.add(JAHAuxSend(this,transGroup,internalBus));
			};
		};
		transfer = JAHTransfer(this);
		transfer.updateOutArray();
		
	}

	updateAuxArray{
			this.sendList.do{|send| 
					send.updateAuxArray(JAHDesk.auxBusDict.keys.asArray.flat);
				};
	}
	remove{
		trackGroup.free;
		internalBus.free;
		^allTracks.remove(this);
	}
	
	saveSettings{
		var insertSettings =[];
		var inputSettings = [];
		
		inputInsertList.do{|insert|
			var setting = insert.saveSettings;
			inputSettings = inputSettings.add(setting);
			};
		insertList.do{|insert|
				var setting = insert.saveSettings;
				insertSettings = insertSettings.add(setting);
			};
		settingsDict = (\trackName:trackName,
			\insertSettings:insertSettings,
			\inputSettings:inputSettings
			);
		Dialog.savePanel({|path|
			settingsDict.writeArchive(path);
			});
	}
	
	getSettings{|path|
		if(path.isNil){
			Dialog.getPaths({|paths|
				paths.do{|path|
					currentPreset = PathName(path).fullPath;
					};
					this.applySettings();
				},{"cancelled".postln;});
		}{
			this.applySettings(path);
		};
	}
	
	applySettings{
		settingsDict = Object.readArchive(currentPreset);
/*		trackName = settingsDict.at(\trackName);*/
		insertList.do{|insert,i|
			insert.loadSettings(settingsDict.at(\insertSettings)[i]);
		};
		inputInsertList.do{|insert,i|
			insert.loadSettings(settingsDict.at(\inputSettings)[i]);
		};
	}
	
	addAux{
		JAHDesk.auxBusDict.put(trackName.asSymbol,inbus);
	}
}

//maybe inheritance...
JAHAuxTrack : JAHTrack{
	
	*new{|parent,params|
		^super.new.initJAHAuxTrack(parent,params);
	}
	
	initJAHAuxTrack{|argparent,argparams|
		desk = argparent;
		type = \aux;
		params = argparams ?? [nil,nil,nil,nil,nil];
		numInserts = params[0] ?? 4;
		numChannels = params[1] ?? 1;
		numSends = params[4] ?? 0;
		outBus = params[2] ?? 0;
		inbus = params[3] ?? Bus.audio(s,1);
		}
}

JAHMasterTrack : JAHTrack{
	
	*new{|parent,params|
		^super.new.initJAHMasterTrack(parent,params);
		}
		
	initJAHMasterTrack{|argparent,argparams|
		desk = argparent;
		type = \master;
		params = argparams ?? [nil,nil,nil,nil,nil];
		numInserts = params[0] ?? 4;
		numChannels = params[1] ?? 2;
		numSends = 0;
		outBus = params[2] ?? 0;
		inbus = desk.masterBus.at(\MainOut);
	}
}