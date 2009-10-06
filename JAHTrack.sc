//TODO Adding things to the tracks array, and all classvar dicts not initializing properly. How do you only initialize on the first instantiation of a class?
// type is \input,\gen,\aux,\master
//params are [numberInserts,numChannels,outBus,inbus]
// defaults to [5,1,0]
JAHTrack{
	classvar <>typeDict,<>busDict,<>allTracks,<tracksinit = false;
	
	var <>track;
	var <>settingsDict;
	var <>currentPreset;
	var <trackGroup,<inputGroup,<insertGroup,<transGroup;
	var <>transOut,<>sendOut;
	var <>internalBus,<>outBus,<>inbus;
	var <>type,<>params;
	var <numChannels,<numInserts,<>insertList;
	var <>inputInsertList;
	var <>trackName,label;
	var s;
	var <>win,<>backgroundView,<>trackView,<>muteButt;
	var <transfer;
	var trackColor;
	
	*new{|type,params|
			^super.new.initJAHTrack(type,params);
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
	
	initJAHTrack{|argType,argparams|
		currentPreset = nil;
		tracksinit = true;
		//boot server if not running
		trackColor = Color.new255(124,24,200);
		s = Server.default;
		params = argparams ? [nil,nil,nil,nil];
		type = argType ? \input;
		numInserts = params[0] ? 4;
		numChannels = params[1] ? 1;
		outBus = params[2] ? 0;
		trackName = "JahTrack";
		trackGroup = Group(s,\addToTail);
		inputGroup = Group(trackGroup,\addToHead);
		insertGroup = Group(inputGroup,\addAfter);
		transGroup = Group(trackGroup,\addToTail);
		internalBus = Bus.audio(s,numChannels);
		inbus = params[3] ?? 8;
		insertList = List.new;
		inputInsertList = List.new;
		transOut = 0;
//last step of initialization, add to tracks array;
		allTracks = allTracks.add(this);
	}
	
	gui{|argparent,argPoint| //parent window for gui,topLeft Point for track View
		win = argparent;
		win.onClose_({JAHTrack.freeAll();});
		trackView = FlowView(win,Rect(argPoint.x,argPoint.y,125,600));
		label = SCStaticText(trackView,Rect(0,0,90,20))
				.font_(Font("Helvetica",12))
				.stringColor_(trackColor)
				.string_(trackName);
		muteButt = RoundButton(trackView,Rect(0,0,20,20))
				.canFocus_(false)
				.extrude_(false)
				.border_(1)
				.states_([[\speaker,Color.green,Color.white],[\speaker,Color.red,Color.white]])
				.action_({|butt|
					if(butt.value==1){
						trackGroup.run(false);
					}{
						trackGroup.run(true);
					};
					});
		SCStaticText(trackView,Rect(0,0,110,20))
				.font_(Font("Helvetica",10))
				.stringColor_(trackColor)
				.string_(type.asString);
		inputInsertList.add(JAHInsert(this,type).id_(type++1).insertGroup_(Group(inputGroup,\addToTail)));

		SCStaticText(trackView,Rect(0,0,110,20))
				.font_(Font("Helvetica",10))
				.stringColor_(trackColor)
				.string_("Effect Inserts");
				
		numInserts.do{|i|
			insertList.add(JAHInsert(this,\insert).id_(\insert++i).insertGroup_(Group(insertGroup,\addToTail)));
		};
		
		
		transfer = JAHTransfer(this);
	}

	updateAuxArray{
		this.transfer.updateAuxArray();
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
	
	*new{|params|
		^super.new.initJAHAuxTrack(params);
	}
	
	initJAHAuxTrack{|argparams|
		
		type = \aux;
		params = argparams ?? [nil,nil,nil,nil];
		numInserts = params[0] ?? 4;
		numChannels = params[1] ?? 1;
		outBus = params[2] ?? 0;
		inbus = params[3] ?? Bus.audio(s,1);
		}
}