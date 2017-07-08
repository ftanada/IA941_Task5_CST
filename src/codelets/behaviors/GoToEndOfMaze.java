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
import java.util.List;
import memory.CreatureInnerSense;
import support.Coordinates;
import support.Environment;
import support.GridMap;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;

public class GoToEndOfMaze extends Codelet 
{
  private MemoryObject bodyMO;
  private MemoryObject knownWallsMO;
  private MemoryObject fuelMO = null;
  private MemoryObject legsMO;
  private MemoryObject selfInfoMO;
  private int creatureBasicSpeed;
  private double reachDistance;
  private Creature myCreature;
  private GridMap myMap;
  private Environment myEnvironment;

  public GoToEndOfMaze(int creatureBasicSpeed, int reachDistance, Environment env, GridMap map) 
  {
    this.creatureBasicSpeed = creatureBasicSpeed;
    this.reachDistance = reachDistance;
    this.setTimeStep(1000);
    myCreature = env.myCreature;
    myEnvironment = env;
    myMap = map;
  }

  @Override
  public void accessMemoryObjects() 
  {
    knownWallsMO = (MemoryObject)this.getInput("KNOWN_WALLS");
    selfInfoMO = (MemoryObject)this.getInput("INNER");
    legsMO = (MemoryObject)this.getOutput("LEGS");
    fuelMO = (MemoryObject) this.getInput("FUEL");
    bodyMO = (MemoryObject)this.getOutput("BODY");
  }

  @Override
  public void proc() 
  {
      // Find distance between creature and closest apple
      //If far, go towards it
      //If close, stops

        List<Thing> walls = (List<Thing>) knownWallsMO.getI();
        CreatureInnerSense cis = (CreatureInnerSense) selfInfoMO.getI();
        if ((walls == null) || (walls.isEmpty()))
          return;
        int i = walls.size();
        if (i <= 1)
          return;
        System.out.println("GoToEndOfMaze.proc: wall not empty "+i);
	if (myMap != null)
	{
            double selfX = cis.position.getX();
	    double selfY = cis.position.getY();
            // FMT checking if already at final destination
            if (selfX > 580 && selfY < 20)
            {
              // reached end
                System.out.println("GoToEndOfMaze.proc: reached target");
                if (myEnvironment != null)
                try {
                  myEnvironment.w.reset();
                } catch (Exception e) 
                  {
                      e.printStackTrace();
                  }
                return;
            }
            myMap.markStartPosition(selfX,selfY);
            List<Coordinates> coord = myMap.findPath();
            if (coord != null && !coord.isEmpty()) try
            {
                myCreature = myEnvironment.myCreature;
                myCreature.moveto(1.5, coord.get(0).getX(), coord.get(0).getY());
            } catch (Exception e) 
              {
                e.printStackTrace();
              }
        }  // end if walls
  } //end proc
        
        @Override
        public void calculateActivation() 
        {
        }

}
