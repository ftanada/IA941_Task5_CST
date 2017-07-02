/*****************************************************************************
 * Copyright 2007-2015 DCA-FEEC-UNICAMP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *    Klaus Raizer, Andre Paraense, Ricardo Ribeiro Gudwin
 *****************************************************************************/

import support.Environment;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;

import codelets.behaviors.EatClosestApple;
import codelets.behaviors.Forage;
import codelets.behaviors.GoToClosestApple;
import codelets.motor.HandsActionCodelet;
import codelets.motor.LegsActionCodelet;
import codelets.perception.AppleDetector;
import codelets.perception.ClosestAppleDetector;
import codelets.sensors.InnerSense;
import codelets.sensors.Vision;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import memory.CreatureInnerSense;
import support.Environment;
import support.MindView;
import ws3dproxy.model.Thing;

import codelets.motor.BodyActionCodelet;
import codelets.behaviors.EatClosestNut;
import codelets.perception.NutDetector;
import codelets.behaviors.GetClosestJewel;
import codelets.behaviors.GoToClosestJewel;
import codelets.behaviors.GoToEndOfMaze;
import codelets.perception.ClosestJewelDetector;
import codelets.perception.JewelDetector;
import codelets.perception.WallDetector;
import support.GridMap;
import ws3dproxy.model.Leaflet;

/**
 *
 * @author rgudwin
 */
public class AgentMind extends Mind 
{    
    private static int creatureBasicSpeed = 3;
    private static int reachDistance = 50;
    
    public AgentMind(Environment env) 
    {
        super();
                
        // Declare Memory Objects        
	MemoryObject legsMO;
	MemoryObject handsMO;
        MemoryObject visionMO;
        MemoryObject innerSenseMO;
        MemoryObject closestAppleMO;
        MemoryObject knownApplesMO;
        // FMT 2017
        MemoryObject closestJewelMO;
        MemoryObject knownJewelsMO;
        MemoryObject closestNutMO;
        MemoryObject knownNutsMO;
        MemoryObject bodyMO;
        MemoryObject fuelMO;
        MemoryObject leafletMO;
        // FMT path
        MemoryObject knownWallsMO;                                
        
        //Initialize Memory Objects
        legsMO = createMemoryObject("LEGS", "");
	handsMO = createMemoryObject("HANDS", "");
        List<Thing> vision_list = Collections.synchronizedList(new ArrayList<Thing>());
	visionMO = createMemoryObject("VISION",vision_list);
        CreatureInnerSense cis = new CreatureInnerSense();
	innerSenseMO = createMemoryObject("INNER", cis);
        Thing closestApple = null;
        closestAppleMO = createMemoryObject("CLOSEST_APPLE", closestApple);
        List<Thing> knownApples = Collections.synchronizedList(new ArrayList<Thing>());
        knownApplesMO = createMemoryObject("KNOWN_APPLES", knownApples);
                
        // FMT 2017 initialize jewel objects
        Thing closestJewel = null;
        closestJewelMO = createMemoryObject("CLOSEST_JEWEL", closestJewel);
        List<Thing> knownJewels = Collections.synchronizedList(new ArrayList<Thing>());
        knownJewelsMO = createMemoryObject("KNOWN_JEWELS", knownJewels);
        
        // FMT handling nuts as well
        Thing closestNut = null;
        closestNutMO = createMemoryObject("CLOSEST_NUT", closestNut);
        List<Thing> knownNuts = Collections.synchronizedList(new ArrayList<Thing>());
        knownNutsMO = createMemoryObject("KNOWN_NUTS", knownNuts);
        
        // handling leaflet
        List<Leaflet> leaflets = env.myCreature.getLeaflets();
        leafletMO = createMemoryObject("LEAFLETS",leaflets);   
        
        // handling energy
        double fuel = env.myCreature.getFuel();
        fuelMO = createMemoryObject("FUEL",fuel);
        
        // handling walls
        List<Thing> knownWalls = Collections.synchronizedList(new ArrayList<Thing>());
        knownWallsMO = createMemoryObject("KNOWN_WALLS", knownWalls);
        
        // FMT alternative to hands + legs = body
        bodyMO = createMemoryObject("BODY", "");
        
        // Create and Populate MindViewer
        MindView mv = new MindView("FMT_MindView");
        mv.addMO(knownApplesMO);
        mv.addMO(visionMO);
        mv.addMO(closestAppleMO);
        mv.addMO(innerSenseMO);
        //mv.addMO(handsMO);
        //mv.addMO(legsMO);
        // FMT 2017
        mv.addMO(closestJewelMO);
        mv.addMO(knownJewelsMO);
        mv.addMO(closestNutMO);
        mv.addMO(knownNutsMO);
        mv.addMO(leafletMO);
        mv.addMO(fuelMO);
        mv.addMO(bodyMO);
                        
        mv.StartTimer();
        mv.setVisible(true);
		
	// Create Sensor Codelets	
	Codelet vision = new Vision(env.myCreature);
	vision.addOutput(visionMO);
        insertCodelet(vision); //Creates a vision sensor
	
	Codelet innerSense = new InnerSense(env.myCreature);
	innerSense.addOutput(innerSenseMO);
        insertCodelet(innerSense); //A sensor for the inner state of the creature
		
	// Create Actuator Codelets
	Codelet legs = new LegsActionCodelet(env.myCreature);
	legs.addInput(legsMO);
        //insertCodelet(legs);

	Codelet hands = new HandsActionCodelet(env.myCreature);
	hands.addInput(handsMO);
        //insertCodelet(hands);
		
        // FMT new body
        Codelet body = new BodyActionCodelet(env.myCreature);
	body.addInput(bodyMO);
        insertCodelet(body);
	
	// Create Perception Codelets
        Codelet ad = new AppleDetector();
        ad.addInput(visionMO);
        ad.addOutput(knownApplesMO);
        insertCodelet(ad);
                
	Codelet closestAppleDetector = new ClosestAppleDetector();
	closestAppleDetector.addInput(knownApplesMO);
	closestAppleDetector.addInput(innerSenseMO);
	closestAppleDetector.addOutput(closestAppleMO);
        insertCodelet(closestAppleDetector);
		
	// Create Behavior Codelets
	Codelet goToClosestApple = new GoToClosestApple(creatureBasicSpeed,reachDistance);
	goToClosestApple.addInput(closestAppleMO);
	goToClosestApple.addInput(innerSenseMO);
        goToClosestApple.addInput(fuelMO);
	//goToClosestApple.addOutput(legsMO);
        goToClosestApple.addOutput(bodyMO);
        //insertCodelet(goToClosestApple);
		
	Codelet eatApple = new EatClosestApple(reachDistance);
	eatApple.addInput(closestAppleMO);
	eatApple.addInput(innerSenseMO);
	//eatApple.addOutput(handsMO);
        eatApple.addOutput(bodyMO);
        eatApple.addOutput(knownApplesMO);
        insertCodelet(eatApple);
                
        //Codelet forage=new Forage();
	//forage.addInput(knownApplesMO);
        //forage.addOutput(legsMO);
        //insertCodelet(forage);
                
        Codelet nd = new NutDetector();
        nd.addInput(visionMO);
        nd.addOutput(knownNutsMO);
        insertCodelet(nd);

        // FMT eating nuts
	Codelet eatNut = new EatClosestNut(reachDistance);
	eatNut.addInput(closestNutMO);
	eatNut.addInput(innerSenseMO);
	//eatNut.addOutput(handsMO);
        eatNut.addOutput(bodyMO);
        eatNut.addOutput(knownNutsMO);
        insertCodelet(eatNut);
                
        // FMT adding jewel handling
        // Create Perception Codelets
        Codelet jd = new JewelDetector();
        jd.addInput(visionMO);
        jd.addOutput(knownJewelsMO);
        insertCodelet(jd);
                
	Codelet closestJewelDetector = new ClosestJewelDetector();
	closestJewelDetector.addInput(knownJewelsMO);
	closestJewelDetector.addInput(innerSenseMO);
	closestJewelDetector.addOutput(closestJewelMO);
        insertCodelet(closestJewelDetector);
	
        // perceiving walls
        GridMap myMap = new GridMap(1,1,780,10);
        Codelet wd = new WallDetector(myMap);
        wd.addInput(visionMO);
        wd.addOutput(knownWallsMO);
        insertCodelet(wd);

	// Create Behavior Codelets
	Codelet goToClosestJewel = new GoToClosestJewel(creatureBasicSpeed,reachDistance);
	goToClosestJewel.addInput(closestJewelMO);
	goToClosestJewel.addInput(innerSenseMO);
        goToClosestJewel.addInput(fuelMO);
	//goToClosestJewel.addOutput(legsMO);
        goToClosestJewel.addOutput(bodyMO);
        insertCodelet(goToClosestJewel);
		
	Codelet getJewel = new GetClosestJewel(reachDistance, env);
	getJewel.addInput(closestJewelMO);
	getJewel.addInput(innerSenseMO);
        getJewel.addInput(leafletMO);
	//getJewel.addOutput(handsMO);
        getJewel.addOutput(bodyMO);
        getJewel.addOutput(knownJewelsMO);
        insertCodelet(getJewel);
                
        Codelet forageJewel = new Forage();
        forageJewel.addInput(knownApplesMO);
	forageJewel.addInput(knownJewelsMO);
        forageJewel.addInput(knownWallsMO);
        forageJewel.addInput(fuelMO);
        //forageJewel.addOutput(legsMO);
        forageJewel.addOutput(bodyMO);
        insertCodelet(forageJewel);

        // for path navigation
       	Codelet goToEnd = new GoToEndOfMaze(creatureBasicSpeed,reachDistance,env,myMap);
	goToEnd.addInput(knownWallsMO);
	goToEnd.addInput(innerSenseMO);
        goToEnd.addInput(fuelMO);
	//goToEnd.addOutput(legsMO);
        goToEnd.addOutput(bodyMO);
        insertCodelet(goToEnd);

        // sets a time step for running the codelets to avoid heating too much your machine
        //for (Codelet c : this.getCodeRack().getAllCodelets())
        //  c.setTimeStep(500);
		
	// Start Cognitive Cycle
	start(); 
    }             
    
}
