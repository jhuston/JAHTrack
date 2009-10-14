JAHDesk{
	classvar <>auxBusDict;
	var <>name;
	var <>bounds;
	var <>win,<>dashboardView,<>tracksView,<>auxTracksView,<>masterTrackView;
	var <>tracks,<>tracksMenu;
	var <>trackSettings;
	var <>server;
	var <>settingsDict;
	var <>currentPreset;
	var <>selectedTrack;
	var <>addTrackButt,<>loadDeskButt;
	var <>masterBus,<>masterGroup,<>tracksGroup,<>auxGroup,<>loopGroup;
	var s;
	var <>presets;

	*new{|name|
		^super.new.initJAHDesk(name);
	}
	
	initJAHDesk{|argname|
		var width,height;
		s = Server.default;
		settingsDict = ();
		auxBusDict = ();
		trackSettings = ();
		tracks = List.new;
		name = argname ?? "JAH Desk";
		bounds = SCWindow.screenBounds;
		width = bounds.width-50;
		height = bounds.height - 300;
		win = SCWindow(name,Rect(20,320,width,height));
		win.view.background_(Color.white);
		win.front;
		win.onClose_({this.cleanUp();});
		dashboardView = FlowView(win,Rect(10,10,200,height)).background_(Color.red);
		tracksView = FlowView(win,Rect(210,10,500,height)).background_(Color.grey(0.9));
		tracksView.decorator.gap_(20@20);
		auxTracksView = FlowView(win,Rect(510,10,width-575,height)).background_(Color.grey(0.8));
		tracksView.decorator.gap_(20@20);
		masterTrackView = FlowView(win,Rect(width-135,10,125,height)).background_(Color.grey(0.7));
		tracksView.decorator.gap_(20@20);
		addTrackButt = RoundButton(dashboardView,Rect(0,0,80,20))
			.font_(Font("Helvetica",10))
			.extrude_(false)
			.border_(1)
			.canFocus_(false)
			.states_([["Add Track",Color.black,Color.white]])
			.mouseDownAction_({|butt|
				this.addTrackDialog();
				});
		if(s.serverRunning.not){
			s.boot;
			s.doWhenBooted({this.buildGroup;});
		}{this.buildGroup;};
		loadDeskButt = RoundButton(dashboardView,Rect(0,0,80,20))
				.font_(Font("Helvetica",10))
				.extrude_(false)
				.border_(1)
				.canFocus_(false)
				.states_([["Load Desk",Color.black,Color.white]])
				.mouseDownAction_({|butt|
					this.getSettings();
				});
		tracksMenu = SCPopUpMenu(dashboardView,Rect(0,0,150,15))
				.font_(Font("Helvetica",9))
				.canFocus_(false)
				.items_(trackSettings.keys.asArray.flat)
				.action_({|menu|
					selectedTrack = menu.item;
					selectedTrack.postln;
					});
					
		presets = JAHDeskPresets(this);
	}
	
	updateTrackMenu{
		^tracksMenu.items_(trackSettings.keys.asArray.flat);
	}
	buildGroup{
		masterBus = (\MainOut:Bus.audio(s,2));
		masterGroup = Group(s,\addToTail);
		loopGroup = Group(masterGroup,\addBefore);
		tracksGroup = Group(s,\addToHead);
		auxGroup = Group(tracksGroup,\addAfter);
	}
	addTrackDialog{
		var tempName="New Track",tempType=\input,tempArgs,modalWin,addButt,cancelButt,tempNumInserts=0,tempNumChannels=1,tempNumSends;
		var typeList,nameInput,insertsNumList,channelsNumList,sendsNumList;
		modalWin = SCWindow("Add New Track",Rect((bounds.width/2)-100,(bounds.height/2)-100,200,200)).front;
		modalWin.addFlowLayout(5@5,5@5);
		SCStaticText(modalWin,Rect(0,0,50,15))
					.string_("type: ");
		typeList = SCPopUpMenu(modalWin,Rect(0,0,100,15))
				.font_(Font("Helvetica",10))
				.items_([\input,\gen,\aux,\master])
				.action_({|menu|
					tempType = menu.item;
				});
		SCStaticText(modalWin,Rect(0,0,50,15))
					.string_("name: ");
		nameInput = SCTextField(modalWin,Rect(0,0,100,15))
					.keyUpAction_({|field|
						tempName = field.value;
					});
/*		modalWin.view.decorator.nextLine;*/
		SCStaticText(modalWin,Rect(0,0,50,15))
					.string_("inserts: ");
		insertsNumList = SCPopUpMenu(modalWin,Rect(0,0,100,15))
					.font_(Font("Helvetica",10))
					.items_([\0,\1,\2,\3,\4,\5,\6])
					.action_({|menu|
						tempNumInserts = menu.item.asFloat;
					});
/*		modalWin.view.decorator.nextLine;*/
		SCStaticText(modalWin,Rect(0,0,50,15))
					.string_("channels: ");
		channelsNumList = SCPopUpMenu(modalWin,Rect(0,0,100,15))
					.font_(Font("Helvetica",10))
					.items_([\1,\2])
					.action_({|menu|
						tempNumChannels = menu.item.asFloat;
					});
		SCStaticText(modalWin,Rect(0,0,50,15))
					.string_("sends: ");
		sendsNumList = SCPopUpMenu(modalWin,Rect(0,0,100,15))
					.font_(Font("Helvetica",10))
					.items_([\0,\1,\2,\3,\4])
					.action_({|menu|
						tempNumSends = menu.item.asFloat;
					});
		modalWin.view.decorator.nextLine;
		addButt = RoundButton(modalWin,Rect(0,0,50,15))
				.font_(Font("Helvetica",10))
				.border_(1)
				.canFocus_(false)
				.extrude_(false)
				.states_([["add",Color.black,Color.white]])
				.action_({|butt|
					tempArgs = [tempNumInserts,tempNumChannels,nil,nil,tempNumSends];
					this.addNewTrack(tempName,tempType,tempArgs);
					modalWin.close;
				});
		cancelButt = RoundButton(modalWin,Rect(0,0,50,15))
				.font_(Font("Helvetica",10))
				.border_(1)
				.canFocus_(false)
				.extrude_(false)
				.states_([["cancel",Color.black,Color.white]])
				.action_({|butt|
					modalWin.close;
				});
		
	}
	

	checkName{|argname|
		var makeName;
		
/*		z = {|argitem| if(v.any({|item| item == argitem})){argitem = PathName(argitem.asString).nextName.asSymbol;z.value(argitem);}{argitem}}*/
		
		makeName = {|argitem|
			 if(tracks.flat.any({|item| item == argitem.asSymbol})){argitem = PathName(argitem.asString).nextName.asSymbol;makeName.value(argitem);
			}{
				argitem;
			};
		};
		^makeName.value(argname);
	}


	addNewTrack{|argname,argtype,trackargs|
		var newTrack;
		[argname,argtype,trackargs].postln;
		argname = this.checkName(argname);
		switch(argtype)
		{\input}
			{newTrack = JAHTrack(this,argtype,trackargs).trackName_(argname);
				newTrack.buildGroup(tracksGroup);
				newTrack.gui(tracksView,0@0);
				newTrack.transfer.outputList.valueAction_(2);
				}
		{\gen}
			{newTrack = JAHTrack(this,argtype,trackargs).trackName_(argname);
				newTrack.buildGroup(tracksGroup);
				newTrack.gui(tracksView,0@0);
				newTrack.transfer.outputList.valueAction_(2);
				}
		{\aux}
			{newTrack = JAHAuxTrack(this,trackargs).trackName_(argname).addAux;
				newTrack.buildGroup(auxGroup);
				newTrack.gui(auxTracksView,0@0);
				newTrack.transfer.outputList.valueAction_(2);
				newTrack.input.listView.valueAction_(2);
			}
		{\master}
			{newTrack = JAHMasterTrack(this,trackargs).trackName_(argname);
				newTrack.buildGroup(masterGroup);
				newTrack.gui(masterTrackView,0@0);
				newTrack.input.listView.valueAction_(2);
			};
			
		trackSettings.put(argname.asSymbol,[argname.asSymbol,argtype,trackargs]);
		tracks = tracks.add([argname.asSymbol,[newTrack]]);
		this.updateTrackMenu();
		if(JAHDesk.auxBusDict.keys.notEmpty){
			this.updateAuxArray();
		};
	}
	
	saveSettings{
		var trackOrder =[];
		tracks.do{|item| trackOrder = trackOrder.add(item[0]);};
		settingsDict = (\deskName:name,
			\trackOrder:trackOrder,
			\tracks:trackSettings
			);
		Dialog.savePanel({|path|
				settingsDict.writeArchive(path);
		});
	}
	getSettings{|path|
		"Load Desk Settings".postln;
		if(path.isNil){
			Dialog.getPaths({|paths|
				paths.do{|path|
					currentPreset = PathName.new(path).fullPath;
				};
				this.applySettings();
			},{"canceled".postln});
		}{
		settingsDict = Object.readArchive(path);
		this.applySettings();
		};
	}
	applySettings{
		settingsDict = Object.readArchive(currentPreset);
		name = settingsDict.at(\deskName);
		settingsDict.at(\trackOrder).do{|track|
			var params = settingsDict.at(\tracks).at(track);
			postf("params:%\n key:%",params,track);
			this.addNewTrack(params[0],params[1],params[2]);
		};
	}
	updateAuxArray{
		this.tracks.do{|track|
			track[1][0].updateAuxArray();
			};
	}
	
	cleanUp{
		tracksGroup.free;
		masterGroup.free;
		auxGroup.free;
		loopGroup.free;
	}

}