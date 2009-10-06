JAHDesk{
	classvar <>auxBusDict;
	var <>name;
	var <>bounds;
	var <>win,<>dashboardView,<>tracksView;
	var <>tracks;
	var <>trackSettings;
	var <>server;
	var <>settingsDict;
	var <>currentPreset;
	var <>addTrackButt;

	*new{|name|
		^super.new.initJAHDesk(name);
	}
	
	initJAHDesk{|argname|
		var width,height;
		settingsDict = ();
		auxBusDict = ();
		trackSettings = ();
		server = Server.default;
		if(server.serverRunning.not){server.boot;};
		tracks = List.new;
		name = argname ?? "JAH Desk";
		bounds = SCWindow.screenBounds;
		width = bounds.width-50;
		height = bounds.height - 300;
		win = SCWindow(name,Rect(20,320,width,height));
		win.view.background_(Color.white);
		win.front;
		dashboardView = FlowView(win,Rect(10,10,200,height));
		tracksView = FlowView(win,Rect(210,10,width-200,height));
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
	}
	
	addTrackDialog{
		var tempName,tempType=\input,tempArgs,modalWin,addButt,cancelButt,tempNumInserts,tempNumChannels;
		var typeList,nameInput,insertsNumBox,channelsNumBox;
		modalWin = SCModalWindow("Add New Track",Rect((bounds.width/2)-100,(bounds.height/2)-100,200,200));
		modalWin.view.decorator = FlowLayout(modalWin.view.bounds);
		typeList = SCPopUpMenu(modalWin,Rect(0,0,100,15))
				.font_(Font("Helvetica",10))
				.items_([\input,\gen,\aux,\master])
				.action_({|menu|
					tempType = menu.item;
				});
		nameInput = SCTextField(modalWin,Rect(0,0,100,15))
					.keyUpAction_({|field|
						tempName = field.value;
					});
		modalWin.view.decorator.nextLine;
		addButt = RoundButton(modalWin,Rect(0,0,50,15))
				.font_(Font("Helvetica",10))
				.border_(1)
				.canFocus_(false)
				.extrude_(false)
				.states_([["add",Color.black,Color.white]])
				.action_({|butt|
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
	addNewTrack{|argname,argtype,trackargs|
		var newTrack;
		switch(argtype)
		{\input}
			{newTrack = JAHTrack(argtype,trackargs).trackName_(argname);}
		{\gen}
			{newTrack = JAHTrack(argtype,trackargs).trackName_(argname);}
		{\aux}
			{newTrack = JAHAuxTrack(trackargs).trackName_(argname).addAux;};
			
		trackSettings.put(argname.asSymbol,[argname.asSymbol,argtype,trackargs]);
		tracks = tracks.add([argname.asSymbol,[newTrack]]);
		newTrack.gui(tracksView,0@0);
		
	}
	
	saveSettings{
		var trackOrder =[];
		tracks.do{|item| trackOrder = trackOrder.add(item[0]);
			
			};
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

}