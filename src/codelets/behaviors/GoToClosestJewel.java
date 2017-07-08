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
import java.util.List;
import memory.CreatureInnerSense;
import ws3dproxy.model.Thing;

public class GoToClosestJewel extends Codelet 
{
    private MemoryObject closestJewelMO;
    private MemoryObject selfInfoMO;
    private MemoryObject legsMO;
    private int creatureBasicSpeed; 
    private double reachDistance;
    double fuel = 0;
    private MemoryObject fuelMO = null;
    private MemoryObject bodyMO;
    private MemoryObject wallsMO;

    public GoToClosestJewel(int creatureBasicSpeed, int reachDistance) 
    {
	this.creatureBasicSpeed = creatureBasicSpeed;
	this.reachDistance = reachDistance;
        this.setTimeStep(1000);
    }

    @Override
    public void accessMemoryObjects() 
    {
	closestJewelMO = (MemoryObject)this.getInput("CLOSEST_JEWEL");
	selfInfoMO = (MemoryObject)this.getInput("INNER");
	legsMO = (MemoryObject)this.getOutput("LEGS");
        bodyMO = (MemoryObject)this.getOutput("BODY");
        fuelMO = (MemoryObject) this.getInput("FUEL");
        wallsMO = (MemoryObject) this.getInput("KNOWN_WALSS");
    }

    @Override
    public void proc() 
    {
        // FMT checking energy level
        if (fuelMO != null)
        {
            fuel = (double) fuelMO.getI();
            if (fuel < 40)
            {
                System.out.println("GoToClosestJewel.proc: abort with low energy "+fuel);
                return;
            }
        }
        
        // FMT check if not in path scenario
        if (wallsMO != null)
        {
          List<Thing> known = (List<Thing>) wallsMO.getI();  
          if (known.size() > 0) 
            return;
        }
        
	// Find distance between creature and closest apple
	//If far, go towards it
	//If close, stops

        Thing closestJewel = (Thing) closestJewelMO.getI();
        CreatureInnerSense cis = (CreatureInnerSense) selfInfoMO.getI();

		if (closestJewel != null)
		{
		    double jewelX = 0;
		    double jewelY = 0;
		    try 
                    {
                        jewelX = closestJewel.getX1();
                        jewelY = closestJewel.getY1();
		    } catch (Exception e) 
                    {
			e.printStackTrace();
		    }

		    double selfX = cis.position.getX();
                    double selfY = cis.position.getY();

		    Point2D pJewel = new Point();
		    pJewel.setLocation(jewelX, jewelY);

		    Point2D pSelf = new Point();
		    pSelf.setLocation(selfX, selfY);

			double distance = pSelf.distance(pJewel);
			JSONObject message=new JSONObject();
			try 
                        {
			   if (distance > reachDistance)
                           { //Go to it
                             message.put("ACTION", "GOTO");
  			     message.put("X", (int)jewelX);
			     message.put("Y", (int)jewelY);
                             message.put("SPEED", creatureBasicSpeed);	
			    } else
                           {//Stop
				message.put("ACTION", "GOTO");
				message.put("X", (int)jewelX);
				message.put("Y", (int)jewelY);
                                message.put("SPEED", 0.0);	
 			    }
                            System.out.println("GoToClosestJewel.proc: "+message.toString());
 			    //legsMO.updateI(message.toString());
                            bodyMO.updateI(message.toString());
			} catch (JSONException e) 
                        {
			    e.printStackTrace();
			}	
		}
	}//end proc
        
        @Override
        public void calculateActivation() 
        {
        
        }

}
