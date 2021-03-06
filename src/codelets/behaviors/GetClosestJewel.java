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
 * Altered by:
 *    Fabio Tanada
 *****************************************************************************/

package codelets.behaviors;

import java.awt.Point;
import java.awt.geom.Point2D;

import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import memory.CreatureInnerSense;
import support.Environment;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import ws3dproxy.CommandExecException;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Leaflet;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;
import ws3dproxy.util.Constants;

public class GetClosestJewel extends Codelet 
{
    private MemoryObject closestJewelMO;
    private MemoryObject innerSenseMO;
    private MemoryObject knownMO;
    private int reachDistance;
    private MemoryObject handsMO;
    Thing closestJewel;
    CreatureInnerSense cis;
    List<Thing> known;
    // FMT checking leaflets
    private MemoryObject bodyMO = null;
    private MemoryObject leafletsMO = null;
    List<Leaflet> leaflets;
    Creature myCreature;
    Environment myEnvironment;
    World myWorld = null;
    boolean notReset = true;
    Random rdX = null;
    Random rdY = null;

    public GetClosestJewel(int reachDistance, Environment env) 
    {
        this.reachDistance = reachDistance;
        this.myCreature = env.myCreature;
        this.myWorld = env.w;
        this.myEnvironment = env;
        this.setTimeStep(100);
        rdX = new Random();
        rdY = new Random();
    }

    @Override
    public void accessMemoryObjects() 
    {
       closestJewelMO = (MemoryObject) this.getInput("CLOSEST_JEWEL");
       innerSenseMO = (MemoryObject) this.getInput("INNER");
       handsMO = (MemoryObject) this.getOutput("HANDS");
       knownMO = (MemoryObject) this.getOutput("KNOWN_JEWELS");
       // FMT leaflets
       bodyMO = (MemoryObject) this.getOutput("BODY");
       leafletsMO = (MemoryObject) this.getInput("LEAFLETS");
    }

        public boolean isInLeaflet(List<Leaflet> leaflets, String jewelColor)
        {
          Boolean belongs = false;
          if (leaflets != null)
          {
            for (Leaflet leaflet: leaflets)                   
            {
                if (leaflet.ifInLeaflet(jewelColor) &&
                    leaflet.getTotalNumberOfType(jewelColor) > 
                        leaflet.getCollectedNumberOfType(jewelColor))
                {
                    System.out.println("isInLeaflet: found leafletJewel");
                    belongs = true;
                    break;
                }
            }          
          }
          return (belongs);
        }
        
        public boolean isLeafletComplete(List<Leaflet> leaflets)
        {
          Boolean complete = true;
          int iTotalLeft = 0;
          if (leaflets != null)
          {
            for (Leaflet leaflet: leaflets)                   
            {
                if (leaflet.ifInLeaflet(Constants.colorRED))
                  iTotalLeft = iTotalLeft + leaflet.getMissingNumberOfType(Constants.colorRED);
                if (leaflet.ifInLeaflet(Constants.colorGREEN))
                  iTotalLeft = iTotalLeft + leaflet.getMissingNumberOfType(Constants.colorGREEN);
                if (leaflet.ifInLeaflet(Constants.colorBLUE))
                  iTotalLeft = iTotalLeft + leaflet.getMissingNumberOfType(Constants.colorBLUE);
                if (leaflet.ifInLeaflet(Constants.colorYELLOW))
                  iTotalLeft = iTotalLeft + leaflet.getMissingNumberOfType(Constants.colorYELLOW);
                if (leaflet.ifInLeaflet(Constants.colorMAGENTA))
                  iTotalLeft = iTotalLeft + leaflet.getMissingNumberOfType(Constants.colorMAGENTA);
                if (leaflet.ifInLeaflet(Constants.colorWHITE))
                  iTotalLeft = iTotalLeft + leaflet.getMissingNumberOfType(Constants.colorWHITE);
            }          
          }
          if (iTotalLeft > 0)
             complete = false;
          return (complete);
        }
        
	@Override
	public void proc() 
        {
            String jewelName = "";
            closestJewel = (Thing) closestJewelMO.getI();
            cis = (CreatureInnerSense) innerSenseMO.getI();
            known = (List<Thing>) knownMO.getI();
            //Find distance between closest apple and self
  	    //If closer than reachDistance, eat the apple
            // FMT retrieving leaflets
            if (leafletsMO != null)
            {
              leaflets = (List<Leaflet>) leafletsMO.getI();
              //System.out.println("GetClosestJewel.proc: received leaflets");
            }
            else 
              leaflets = null;
                
            // FMT check if leaflet is compete (if yes, halt)
            if (isLeafletComplete(leaflets))
            {
               System.out.println("GetClosestJewel.proc: leaflets completed.");
               if (myCreature != null)
               {    
                 try 
                 {
                     if (myWorld != null && notReset)
                     {
                       notReset = false;
                       //myWorld.reset();
                       myWorld.createBrick(2,10.0,580.0,100.00,590.0);
                       myWorld.createBrick(2,230.0,6.0,240.00,400.0);
                       myWorld.createBrick(2,450.0,380.0,460.00,594.00);
                       myWorld.createBrick(2,630.0,6.0,640.00,370.0);            
                       myWorld.createBrick(2,100.0,300.0,240.00,310.0);
                       //WS3DProxy proxy = new WS3DProxy();
                       //myCreature = proxy.createCreature(10,50,0);
                       //myEnvironment.myCreature = myCreature;
                       //myCreature.start();
                     }
                     //myCreature.stop(); 
                 }
                 catch (Exception e) { e.printStackTrace(); }
               }
               DestroyClosestJewel();
               return;
            }
            
	    if (closestJewel != null)
	    {
		double jewelX = 0;
		double jewelY = 0;
		try 
                {
		   jewelX = closestJewel.getX1();
		   jewelY = closestJewel.getY1();
                   jewelName = closestJewel.getName();                                
		} catch (Exception e) 
                {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
		}

		double selfX = cis.position.getX();
		double selfY = cis.position.getY();

		Point2D pJewel = new Point();
		pJewel.setLocation(jewelX, jewelY);

		Point2D pSelf = new Point();
		pSelf.setLocation(selfX, selfY);

		double distance = pSelf.distance(pJewel);
		JSONObject message = new JSONObject();
		try 
                {
		    if (distance < reachDistance)
                    { //gett or hide it, depends on leaflet						
			message.put("OBJECT", jewelName);
                        if (leaflets != null)
                        {
                          if (isInLeaflet(leaflets,closestJewel.getMaterial().getColorName()))
                          {
		             message.put("ACTION", "SACKIT");
                             System.out.println("GetClosestJewel.proc:  sacking "+jewelName);
                          }
                          else
                          {
                             message.put("ACTION", "HIDEIT");
                             System.out.println("GetClosestJewel.proc:  hiding "+jewelName);
                          }
                        }
                        else
                        {
                            message.put("ACTION", "SACKIT");
                        }
                        System.out.println("GetClosestJewel.proc: "+message.toString());
		        //handsMO.updateI(message.toString());
                        bodyMO.updateI(message.toString());
                        DestroyClosestJewel();
                        // FMT feed another jewel into world
                        if (myWorld != null)
                        {
                            double cX = rdX.nextDouble() * 700;
                            double cY = rdY.nextDouble() * 500;
                            //myWorld.createJewel(1, cX, cY);
                            cX = rdX.nextDouble() * 700;
                            cY = rdY.nextDouble() * 500;
                            //myWorld.createJewel(2, cX, cY);
                        }
	            } else
                    {
			//handsMO.updateI("");	//nothing
                        bodyMO.updateI("");
		    }
				
//		    System.out.println(message);
		} catch (JSONException e) 
                {
		  // TODO Auto-generated catch block
		  e.printStackTrace();
		} //catch (CommandExecException ex) {
                    //Logger.getLogger(GetClosestJewel.class.getName()).log(Level.SEVERE, null, ex);
                //}
	} else
        {
	   //handsMO.updateI("");	//nothing
           bodyMO.updateI("");
        }
        //System.out.println("Before: "+known.size()+ " "+known);
        
        //System.out.println("After: "+known.size()+ " "+known);
	//System.out.println("EatClosestApple: "+ bodyMO.getInfo());	

	}
        
        @Override
        public void calculateActivation() 
        {
        }
        
        public void DestroyClosestJewel() 
        {
           int r = -1;
           int i = 0;
           synchronized(known) 
           {
             CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);  
             for (Thing t : known) 
             {
               if (closestJewel != null) 
                 if (t.getName().equals(closestJewel.getName())) r = i;
              i++;
             }   
             if (r != -1) known.remove(r);
             closestJewel = null;
           }
        }
}
