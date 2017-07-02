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

package codelets.behaviors;

import java.awt.Point;
import java.awt.geom.Point2D;

import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import memory.CreatureInnerSense;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import ws3dproxy.model.Thing;

public class EatClosestNut extends Codelet 
{
    private MemoryObject closestNutMO;
    private MemoryObject innerSenseMO;
    private MemoryObject knownMO;
    private int reachDistance;
    private MemoryObject handsMO;
    Thing closestNut;
    CreatureInnerSense cis;
    List<Thing> known;

    public EatClosestNut(int reachDistance) 
    {
        setTimeStep(100);
	this.reachDistance=reachDistance;
    }

    @Override
    public void accessMemoryObjects() 
    {
	closestNutMO = (MemoryObject)this.getInput("CLOSEST_NUT");
	innerSenseMO = (MemoryObject)this.getInput("INNER");
	handsMO = (MemoryObject)this.getOutput("HANDS");
        knownMO = (MemoryObject)this.getOutput("KNOWN_NUTS");
    }

    @Override
    public void proc() 
    {
        String nutName = "";
        closestNut = (Thing) closestNutMO.getI();
        cis = (CreatureInnerSense) innerSenseMO.getI();
        known = (List<Thing>) knownMO.getI();
        //Find distance between closest apple and self
	//If closer than reachDistance, eat the apple
		
	if (closestNut != null)
	{
	  double nutX=0;
	  double nutY=0;
	  try 
          {
		nutX=closestNut.getX1();
		nutY=closestNut.getY1();
                nutName = closestNut.getName();                                
	  } catch (Exception e) 
          {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }

	  double selfX=cis.position.getX();
	  double selfY=cis.position.getY();

	  Point2D pNut = new Point();
	  pNut.setLocation(nutX, nutY);

	  Point2D pSelf = new Point();
	  pSelf.setLocation(selfX, selfY);

	  double distance = pSelf.distance(pNut); 
	  JSONObject message=new JSONObject();
	  try 
          {
	    if (distance<reachDistance){ //eat it						
	    message.put("OBJECT", nutName);
	    message.put("ACTION", "EATIT");
            System.out.println("EatClosestNut.proc: "+message.toString());
	    handsMO.updateI(message.toString());
            DestroyClosestNut();
	    } else
            {
		handsMO.updateI("");	//nothing
	    }
				
//				System.out.println(message);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			handsMO.updateI("");	//nothing
		}
        //System.out.println("Before: "+known.size()+ " "+known);
        
        //System.out.println("After: "+known.size()+ " "+known);
	//System.out.println("EatClosestApple: "+ handsMO.getInfo());	

	}
        
        @Override
        public void calculateActivation() 
        {        
        }
        
        public void DestroyClosestNut() 
        {
           int r = -1;
           int i = 0;
           synchronized(known) {
             CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);  
             for (Thing t : known) {
              if (closestNut != null) 
                 if (t.getName().equals(closestNut.getName())) r = i;
              i++;
             }   
             if (r != -1) known.remove(r);
             closestNut = null;
           }
        }

}
