/* *****************************************
 * CSCI205 - Software Engineering and Design
 * Spring2022
 * Instructor: Brian King
 * Section: 1 - 10 am
 *
 * Name: Warren Wang
 * Date: 01/26/2022
 *
 * Lab / Assignment:
 *
 * Description:
 *
 * *****************************************/



package main.javafx;

import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import main.BattleMacro;
import main.MovesInventory;
import main.UserInput;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import static java.lang.System.exit;

public class GuiController {
    private BattleView battleView;
    private ChoosePokemonView choosePokemonView;
    private StartGameView startGameView;
    private ForfeitAndEndView forfeitView;
    private RuleView ruleView;
    private ForfeitAndEndView endGameView;

    private Stage primaryStage;
    private Scene battleScene;
    private Scene choosePokemonScene;
    private Scene startScene;
    private Scene forfeitScene;
    private Scene ruleScene;
    private Scene endGameScene;
    private MovesInventory movesInventory;
    private int difficultyLevel;

    private Model model;
    private ByteArrayOutputStream newSysOut;
    private BattleMacro battleMacro;

    public BattleMacro getBattleMacro(){return battleMacro;}
    public BattleView getBattleView(){return battleView;}

    public GuiController(BattleView battleView, Stage primaryStage) throws Exception{
        // before creating out main game, change sys.out to something we capture
        PrintStream sysOutOrig = System.out;
        newSysOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(newSysOut));

        movesInventory = new MovesInventory();


        // create model
        battleMacro = new BattleMacro();
        model = new Model(battleMacro);

        // create all the views
        startGameView = new StartGameView();
        this.battleView = battleView;
        choosePokemonView = new ChoosePokemonView(battleMacro.getBattleMicro().getUserTeam());
        forfeitView = new ForfeitAndEndView();
        ruleView = new RuleView();
        endGameView = new ForfeitAndEndView();

        // create scenes from the views
        startScene = new Scene(startGameView.getRoot());
        battleScene = new Scene(battleView.getRoot());
        choosePokemonScene = new Scene(choosePokemonView.getRoot());
        forfeitScene = new Scene(forfeitView.getRoot());
        ruleScene = new Scene(ruleView.getRoot());
        endGameScene = new Scene(endGameView.getRoot());

        // start the game with the StartGameScene
        this.primaryStage = primaryStage;
        this.primaryStage.setScene(startScene);
        this.primaryStage.sizeToScene();
        this.primaryStage.show();

        this.primaryStage.setTitle("Pokemon BattleFactory Simulator");

        this.primaryStage.setResizable(false);

        model.run();
        initEventHandler();
    }

    public void initEventHandler() {
        // ------- event handler to switch scenes on the main stage -------

        // ---------- START GAME ------------------

        // normal difficulty start game
        startGameView.getStartGameNormal().setOnMouseClicked(event -> {
            difficultyLevel = 1;
            updateChoosePokemonScene();
            // switch to choosepokemonscene
            primaryStage.setScene(choosePokemonScene);
        });

        // hard difficulty start game
        startGameView.getStartGameHard().setOnMouseClicked(event -> {
            difficultyLevel = 2;
            updateChoosePokemonScene();
            // switch to choosepokemonscene
            primaryStage.setScene(choosePokemonScene);
        });

        // quit game
        startGameView.getExit_btn().setOnMouseClicked(event -> {
            // terminate the program
            exit(0);

        });

        // show rules
        startGameView.getRule().setOnMouseClicked(event -> {
            // switch to rule scene (rules are hardcoded in)
            primaryStage.setScene(ruleScene);
        });


        // -------------------- BATTLE VIEW -------------------

        // Attack
        battleView.getAttack().setOnMouseClicked(event -> {
            UserInput.setUSERINPUT("attack");
            UserInput.setCanGetUSERINPUT(true);

            try{TimeUnit.MILLISECONDS.sleep(100);}catch(Exception e){};

            // show moves box
            battleView.bottomRightBoxToggleChoices(0);
        });

        // Switch
        battleView.getSwitch().setOnMouseClicked(event -> {
            // if player has a pokemon alive to switch to
            if(battleMacro.getBattleMicro().getUser().getPokemonTeam().get(1).getIsAlive() || battleMacro.getBattleMicro().getUser().getPokemonTeam().get(2).getIsAlive()) {
                UserInput.setUSERINPUT("Switch");
                UserInput.setCanGetUSERINPUT(true);


                // show what pokemon user can choose from
                try {TimeUnit.MILLISECONDS.sleep(100);} catch (Exception e) {}

                battleView.updateBottomLeftTextBox(newSysOut.toString());

                // switch to the moves buttons and rename them with the names of the remaining
                //   pokemon that user can choose from
                battleView.updateSwitchPoke(battleMacro.getBattleMicro().getUserTeam().get(1).getName(), battleMacro.getBattleMicro().getUserTeam().get(1).getIsAlive(), battleMacro.getBattleMicro().getUserTeam().get(2).getName(), battleMacro.getBattleMicro().getUserTeam().get(2).getIsAlive());
                battleView.bottomRightBoxToggleChoices(2);
            } else {
                // no pokemon alive to switch to, tell user
                System.out.println("You have no alive Pokemon left to switch to.");
            }
        });

        // Forfeit
        battleView.getForfeit().setOnMouseClicked(event -> {
            newSysOut.reset();
            UserInput.setUSERINPUT("forfeit");
            UserInput.setCanGetUSERINPUT(true);

            // tell user if they really want to forfeit
            battleView.updateBottomLeftTextBox(newSysOut.toString());

            // transition to end scene
            try{TimeUnit.MILLISECONDS.sleep(100);}catch(Exception e){};
            primaryStage.setScene(forfeitScene);
            forfeitView.updateTextArea(newSysOut.toString());
        });


        // Move1
        battleView.getMove1().setOnMouseClicked(event -> {
            newSysOut.reset();
            UserInput.setUSERINPUT(battleMacro.getBattleMicro().getUser().getCurrPokemon().getMove(0));
            UserInput.setCanGetUSERINPUT(true);

            UserInput.waitPlayerDied();
            try {TimeUnit.MILLISECONDS.sleep(100);} catch (Exception e) {}

            // check if all of user's pokemon are dead, then switch to end scene
            if(battleMacro.getBattleMicro().getGameOverStatus()){
                endGameView.updateTextArea(newSysOut.toString());
                primaryStage.setScene(endGameScene);
            }

            if(UserInput.getNeedToSwitch()){
                battleView.getPlayerHpBar().setWidth(0); // set hp bar to zero
                battleView.updateSwitchPoke(battleMacro.getBattleMicro().getUserTeam().get(1).getName(), battleMacro.getBattleMicro().getUserTeam().get(1).getIsAlive(), battleMacro.getBattleMicro().getUserTeam().get(2).getName(), battleMacro.getBattleMicro().getUserTeam().get(2).getIsAlive());
                battleView.bottomRightBoxToggleChoices(2);
                UserInput.setNeedToSwitch(false);
            } else {
                UserInput.setNeedToSwitch(false);


                // show results of battle outcome in text area
                try{TimeUnit.MILLISECONDS.sleep(100);}catch(Exception e){};

                battleView.updateBottomLeftTextBox(newSysOut.toString());

                // toggle to show 3 moves after fight
                battleView.bottomRightBoxToggleChoices(1);

                // update battle view's sprites and names
                updateBattleView();
            }
        });

        // Move2
        battleView.getMove2().setOnMouseClicked(event -> {
            newSysOut.reset();
            UserInput.setUSERINPUT(battleMacro.getBattleMicro().getUser().getCurrPokemon().getMove(1));
            UserInput.setCanGetUSERINPUT(true);

            UserInput.waitPlayerDied();
            try {TimeUnit.MILLISECONDS.sleep(100);} catch (Exception e) {}

            // check if all of user's pokemon are dead, then switch to end scene
            if(battleMacro.getBattleMicro().getGameOverStatus()){
                endGameView.updateTextArea(newSysOut.toString());
                primaryStage.setScene(endGameScene);
            }

            if(UserInput.getNeedToSwitch()){
                battleView.getPlayerHpBar().setWidth(0); // set hp bar to zero
                battleView.updateSwitchPoke(battleMacro.getBattleMicro().getUserTeam().get(1).getName(), battleMacro.getBattleMicro().getUserTeam().get(1).getIsAlive(), battleMacro.getBattleMicro().getUserTeam().get(2).getName(), battleMacro.getBattleMicro().getUserTeam().get(2).getIsAlive());
                battleView.bottomRightBoxToggleChoices(2);
                UserInput.setNeedToSwitch(false);
            } else {
                // show results of battle outcome in text area
                try{TimeUnit.MILLISECONDS.sleep(100);}catch(Exception e){};

                battleView.updateBottomLeftTextBox(newSysOut.toString());

                // toggle to show 3 moves after fight
                battleView.bottomRightBoxToggleChoices(1);

                // update battle view's sprites and names
                updateBattleView();
            }
        });

        // Move3
        battleView.getMove3().setOnMouseClicked(event -> {
            newSysOut.reset();
            UserInput.setUSERINPUT(battleMacro.getBattleMicro().getUser().getCurrPokemon().getMove(2));
            UserInput.setCanGetUSERINPUT(true);
            UserInput.waitPlayerDied();
            try {TimeUnit.MILLISECONDS.sleep(100);} catch (Exception e) {}

            // check if all of user's pokemon are dead, then switch to end scene
            if(battleMacro.getBattleMicro().getGameOverStatus()){
                endGameView.updateTextArea(newSysOut.toString());
                primaryStage.setScene(endGameScene);
            }

            if(UserInput.getNeedToSwitch()){
                battleView.getPlayerHpBar().setWidth(0); // set hp bar to zero
                battleView.updateSwitchPoke(battleMacro.getBattleMicro().getUserTeam().get(1).getName(), battleMacro.getBattleMicro().getUserTeam().get(1).getIsAlive(), battleMacro.getBattleMicro().getUserTeam().get(2).getName(), battleMacro.getBattleMicro().getUserTeam().get(2).getIsAlive());
                battleView.bottomRightBoxToggleChoices(2);
                UserInput.setNeedToSwitch(false);
            } else {
                // show results of battle outcome in text area
                try{TimeUnit.MILLISECONDS.sleep(100);}catch(Exception e){};

                battleView.updateBottomLeftTextBox(newSysOut.toString());

                // toggle to show 3 moves after fight
                battleView.bottomRightBoxToggleChoices(1);

                // update battle view's sprites and names
                updateBattleView();
            }
        });

        // Move4
        battleView.getMove4().setOnMouseClicked(event -> {
            newSysOut.reset();
            UserInput.setUSERINPUT(battleMacro.getBattleMicro().getUser().getCurrPokemon().getMove(3));
            UserInput.setCanGetUSERINPUT(true);
            UserInput.waitPlayerDied();
            try {TimeUnit.MILLISECONDS.sleep(100);} catch (Exception e) {}

            // check if all of user's pokemon are dead, then switch to end scene
            if(battleMacro.getBattleMicro().getGameOverStatus()){
                endGameView.updateTextArea(newSysOut.toString());
                primaryStage.setScene(endGameScene);
            }

            if(UserInput.getNeedToSwitch()){
                battleView.getPlayerHpBar().setWidth(0); // set hp bar to zero
                battleView.updateSwitchPoke(battleMacro.getBattleMicro().getUserTeam().get(1).getName(), battleMacro.getBattleMicro().getUserTeam().get(1).getIsAlive(), battleMacro.getBattleMicro().getUserTeam().get(2).getName(), battleMacro.getBattleMicro().getUserTeam().get(2).getIsAlive());
                battleView.bottomRightBoxToggleChoices(2);
                UserInput.setNeedToSwitch(false);
            } else {
                // show results of battle outcome in text area
                try{TimeUnit.MILLISECONDS.sleep(100);}catch(Exception e){};

                battleView.updateBottomLeftTextBox(newSysOut.toString());

                // toggle to show 3 moves after fight
                battleView.bottomRightBoxToggleChoices(1);

                // update battle view's sprites and names
                updateBattleView();
            }


        });

        // Switch to other Poke0
        battleView.getPoke0Btn().setOnMouseClicked(event -> {
            if(battleMacro.getBattleMicro().getUserTeam().get(1).getIsAlive()) {
                // if alive, switch to that pokemon
                UserInput.setUSERINPUT(battleMacro.getBattleMicro().getUserTeam().get(1).getID());
                UserInput.setCanGetUSERINPUT(true);

                try {TimeUnit.MILLISECONDS.sleep(100);} catch (Exception e) {}
                // after switch, bot will attack me, check if need to switch if user gets one shot
                if(UserInput.getNeedToSwitch()){
                    battleView.getPlayerHpBar().setWidth(0); // set hp bar to zero
                    battleView.updateSwitchPoke(battleMacro.getBattleMicro().getUserTeam().get(1).getName(), battleMacro.getBattleMicro().getUserTeam().get(1).getIsAlive(), battleMacro.getBattleMicro().getUserTeam().get(2).getName(), battleMacro.getBattleMicro().getUserTeam().get(2).getIsAlive());
                    battleView.bottomRightBoxToggleChoices(2);
                    UserInput.setNeedToSwitch(false);
                } else {

                    // update text area
                    try {TimeUnit.MILLISECONDS.sleep(100);} catch (Exception e) {}

                    battleView.updateBottomLeftTextBox(newSysOut.toString());
                    newSysOut.reset();

                    // switch box back to 3 choices
                    battleView.bottomRightBoxToggleChoices(1);

                    // update battle view's sprites and names
                    updateBattleView();

                    // check if all of user's pokemon are dead, then switch to end scene
                    if (battleMacro.getBattleMicro().getGameOverStatus()) {
                        endGameView.updateTextArea(newSysOut.toString());
                        primaryStage.setScene(endGameScene);
                    }
                }
            }
            // if dead, do nothing
        });

        // Switch to other Poke1
        battleView.getPoke1Btn().setOnMouseClicked(event -> {
            if(battleMacro.getBattleMicro().getUserTeam().get(2).getIsAlive()) {
                // if alive, switch to that pokemon
                UserInput.setUSERINPUT(battleMacro.getBattleMicro().getUserTeam().get(2).getID());
                UserInput.setCanGetUSERINPUT(true);

                try {TimeUnit.MILLISECONDS.sleep(100);} catch (Exception e) {}

                // after switch, bot will attack me, check if need to switch if user gets one shot
                if(UserInput.getNeedToSwitch()){
                    battleView.getPlayerHpBar().setWidth(0); // set hp bar to zero
                    battleView.updateSwitchPoke(battleMacro.getBattleMicro().getUserTeam().get(1).getName(), battleMacro.getBattleMicro().getUserTeam().get(1).getIsAlive(), battleMacro.getBattleMicro().getUserTeam().get(2).getName(), battleMacro.getBattleMicro().getUserTeam().get(2).getIsAlive());
                    battleView.bottomRightBoxToggleChoices(2);
                    UserInput.setNeedToSwitch(false);
                } else {

                    // update text area
                    try {TimeUnit.MILLISECONDS.sleep(100);} catch (Exception e) {}

                    battleView.updateBottomLeftTextBox(newSysOut.toString());
                    newSysOut.reset();

                    // switch box back to 3 choices
                    battleView.bottomRightBoxToggleChoices(1);

                    // update battle view's sprites and names
                    updateBattleView();

                    // check if all of user's pokemon are dead, then switch to end scene
                    if (battleMacro.getBattleMicro().getGameOverStatus()) {
                        endGameView.updateTextArea(newSysOut.toString());
                        primaryStage.setScene(endGameScene);
                    }
                }
            }
            // if dead, do nothing
        });




        // ----------- CHOOSE POKEMON VIEW -------------

        // confirm select curr viewing pokemon
        choosePokemonView.getCheckMark().setOnMouseClicked(event -> {
            UserInput.setUSERINPUT(choosePokemonView.getCurrPokemonID());

            UserInput.setCanGetUSERINPUT(true);
            choosePokemonView.incrementChosenPokemonCounter();

            // remove chosen pokemon from being able to be chosen again
            choosePokemonView.getChooseFromPoke().remove(choosePokemonView.getChooseFromPoke().get(choosePokemonView.getCurrPokeInd()));
            choosePokemonView.getAllPokeImgs().remove(choosePokemonView.getCurrPokeInd());

            // increment counter, then set curr pokemon to be where the new index is at
            choosePokemonView.incCurrPokeInd();

            choosePokemonView.getMoveDesc().setText(newSysOut.toString());
            updateChoosePokemonScene();

            choosePokemonView.getMoveDesc().setText(newSysOut.toString());


            // check if need to switch scene when 3 pokemon are chosen
            if(choosePokemonView.getPokemonChosenCounter() == 3){
                newSysOut.reset();
                // update the battlescene with player's curr pokemon
                // sleep to allow botteam to update
                try{TimeUnit.MILLISECONDS.sleep(100);}catch(Exception e){}
                // update battle view's sprites and names (player and bot will have been constructed here)
                updateBattleView();
                // TODO update the bot difficulty based on the gameMode selected
                battleMacro.getBattleMicro().getBot().setDifficulty(difficultyLevel);

                // change to battlescene
                primaryStage.setScene(battleScene);
            }
        });

        // left arrow cycle pokemon, subtract index, wrap around if needed
        choosePokemonView.getLeftArrow().setOnMouseClicked(event -> {
            // decrement counter, then set curr pokemon to be where the new index is at
            choosePokemonView.decCurrPokeInd();
            updateChoosePokemonScene();
        });

        // right arrow cycle pokemon, add index, wrap around if neeeded
        choosePokemonView.getRightArrow().setOnMouseClicked(event -> {
            // increment counter, then set curr pokemon to be where the new index is at
            choosePokemonView.incCurrPokeInd();
            updateChoosePokemonScene();
        });


        // Curr Viewing Poke desc
        choosePokemonView.getCurrViewPokemon().setOnMouseClicked(event -> {
            String PokeDes = choosePokemonView.getChooseFromPoke().get(choosePokemonView.getCurrPokeInd()).toSmallString();
            newSysOut.reset();
            System.out.println(PokeDes);
            choosePokemonView.getMoveDesc().clear();
            choosePokemonView.getMoveDesc().appendText(newSysOut.toString());
            choosePokemonView.getMoveDesc().setScrollTop(Double.MAX_VALUE);
        });


        // show move 1 description
        choosePokemonView.getMove1().setOnMouseClicked(event -> {
            String moveName = choosePokemonView.getChooseFromPoke().get(choosePokemonView.getCurrPokeInd()).getMove(0);
            newSysOut.reset();
            System.out.println(movesInventory.getMove(moveName));
            choosePokemonView.getMoveDesc().clear();
            choosePokemonView.getMoveDesc().appendText(newSysOut.toString());
            choosePokemonView.getMoveDesc().setScrollTop(Double.MAX_VALUE);
        });

        // show move 2 description
        choosePokemonView.getMove2().setOnMouseClicked(event -> {

            String moveName = choosePokemonView.getChooseFromPoke().get(choosePokemonView.getCurrPokeInd()).getMove(1);
            newSysOut.reset();
            System.out.println(movesInventory.getMove(moveName));
            choosePokemonView.getMoveDesc().clear();
            choosePokemonView.getMoveDesc().appendText(newSysOut.toString());
            choosePokemonView.getMoveDesc().setScrollTop(Double.MAX_VALUE);
        });

        // show move 3 description
        choosePokemonView.getMove3().setOnMouseClicked(event -> {
            String moveName = choosePokemonView.getChooseFromPoke().get(choosePokemonView.getCurrPokeInd()).getMove(2);
            newSysOut.reset();
            System.out.println(movesInventory.getMove(moveName));
            choosePokemonView.getMoveDesc().clear();
            choosePokemonView.getMoveDesc().appendText(newSysOut.toString());
            choosePokemonView.getMoveDesc().setScrollTop(Double.MAX_VALUE);
        });

        // show move 4 description
        choosePokemonView.getMove4().setOnMouseClicked(event -> {
            String moveName = choosePokemonView.getChooseFromPoke().get(choosePokemonView.getCurrPokeInd()).getMove(3);
            newSysOut.reset();
            System.out.println(movesInventory.getMove(moveName));
            choosePokemonView.getMoveDesc().clear();
            choosePokemonView.getMoveDesc().appendText(newSysOut.toString());
            choosePokemonView.getMoveDesc().setScrollTop(Double.MAX_VALUE);
        });

        // exit button returns player to the start game scene
        choosePokemonView.getExitBtn().setOnMouseClicked(event -> {
            // switch to the starting scene
            UserInput.setUSERINPUT("exit");
            UserInput.setCanGetUSERINPUT(true);

            // reset pokemon view pointers (if go back to choosepokescene, pointer will be 0)
            choosePokemonView.resetPointers();

            // switch to loading screen, wait, and do stuff before switching back to original scene

            try{TimeUnit.MILLISECONDS.sleep(2000);}catch(Exception e){}

            // update choosepokemonview with new team of 6 generated from battlemacro
            choosePokemonView.setOriginalPoketeamAndCleanChooseFromPoke(battleMacro.getBattleMicro().getUserTeam());

            // switch to start scene
            primaryStage.setScene(startScene);
        });

        // ------- Forfeit Scene -------
        // confirm forfeit
        forfeitView.getYesBtn().setOnMouseClicked(event -> {
            // send y to sys.in
            UserInput.setUSERINPUT("y");
            UserInput.setCanGetUSERINPUT(true);

            // switch to end game scene and prompt user if they want to play again
            newSysOut.reset();
            System.out.println("Do you want to play again? (y/n)");
            endGameView.updateTextArea(newSysOut.toString());
            primaryStage.setScene(endGameScene);
        });

        // decline forfeit
        forfeitView.getNoBtn().setOnMouseClicked(event -> {
            // send n to sys.in
            UserInput.setUSERINPUT("n");
            UserInput.setCanGetUSERINPUT(true);

            // go to battleview
            primaryStage.setScene(battleScene);
        });

        // -------- rule scene -------
        // return to start scene
        ruleView.getBackButton().setOnMouseClicked(event -> {
            primaryStage.setScene(startScene);
        });

        // ------- end game scene ------
        // confirm play again
        endGameView.getYesBtn().setOnMouseClicked(event -> {
            // send y to sys.in
            UserInput.setUSERINPUT("y");
            UserInput.setCanGetUSERINPUT(true);

            // wait
            try{TimeUnit.MILLISECONDS.sleep(2200);}catch(Exception e){}

            // update choosepokemonview with new team of 6 generated from battlemacro
            choosePokemonView.setOriginalPoketeamAndCleanChooseFromPoke(battleMacro.getBattleMicro().getUserTeam());
            // update choosefrompokemon scene (should display 6 new random poke to choose from)
            updateChoosePokemonScene();
            // switch to scene
            newSysOut.reset();
            primaryStage.setScene(choosePokemonScene);
        });

        // decline play again, end of game
        endGameView.getNoBtn().setOnMouseClicked(event -> {
            // send n to sys.in
            newSysOut.reset();
            UserInput.setUSERINPUT("n");
            UserInput.setCanGetUSERINPUT(true);

            // refresh textArea
            try{TimeUnit.MILLISECONDS.sleep(100);}catch(Exception e){};

            battleMacro.printExitGameMessage();
            endGameView.updateTextArea(newSysOut.toString()); // print end game stats

            // show pic of clown if player is bad
            double winRate = battleMacro.getWinRate();
            // show different label of text with a special message depending on winrate
            if(winRate < 0.5){
                endGameView.getlevelOfPlayerMsg().setText("Try Harder.");
                endGameView.getlevelOfPlayerMsg().setFont(Font.font("Verdana", FontWeight.NORMAL, 20));
                endGameView.getRoot().getChildren().add(endGameView.getlevelOfPlayerMsg());
                endGameView.getRoot().getChildren().add(endGameView.getClown());
            } else if(winRate >= 0.5 && winRate <= 0.6){
                endGameView.getlevelOfPlayerMsg().setText("Average Player.");
                endGameView.getlevelOfPlayerMsg().setFont(Font.font("Verdana", FontWeight.NORMAL, 30));
                endGameView.getRoot().getChildren().add(endGameView.getlevelOfPlayerMsg());
            } else if(winRate > 0.6 && winRate <= 0.7){
                endGameView.getlevelOfPlayerMsg().setText("Decent Player.");
                endGameView.getlevelOfPlayerMsg().setFont(Font.font("Verdana", FontWeight.NORMAL, 40));
                endGameView.getRoot().getChildren().add(endGameView.getlevelOfPlayerMsg());
            }else if(winRate > 0.7 && winRate <= 0.8){
                endGameView.getlevelOfPlayerMsg().setText("Advanced Player.");
                endGameView.getlevelOfPlayerMsg().setFont(Font.font("Verdana", FontWeight.NORMAL, 50));
                endGameView.getRoot().getChildren().add(endGameView.getlevelOfPlayerMsg());
            }else if(winRate > 0.8 && winRate <= 0.9){
                endGameView.getlevelOfPlayerMsg().setText("Master Player.");
                endGameView.getlevelOfPlayerMsg().setFont(Font.font("Verdana", FontWeight.NORMAL, 60));
                endGameView.getRoot().getChildren().add(endGameView.getlevelOfPlayerMsg());
            }else if(winRate > 0.9 && winRate <= 1){
                endGameView.getlevelOfPlayerMsg().setText("Legendary Player.");
                endGameView.getlevelOfPlayerMsg().setFont(Font.font("Verdana", FontWeight.NORMAL, 40));
                endGameView.getRoot().getChildren().add(endGameView.getlevelOfPlayerMsg());
            }

            // remove all buttons in this end scene, player only can exit now.
            endGameView.getYesBtn().setDisable(true);
            endGameView.getYesBtn().setVisible(false);

            endGameView.getNoBtn().setDisable(true);
            endGameView.getNoBtn().setVisible(false);
        });

    }

    /**
     * Updates the view of the choosePokemonScene
     */
    private void updateChoosePokemonScene() {
        // update choosepokemon scene
        choosePokemonView.updateCurrViewPokemon();
        choosePokemonView.setAllMoves();
    }

    /**
     * update Battle View's sprites and names
     */
    public void updateBattleView(){
        try{TimeUnit.MILLISECONDS.sleep(100);}catch(Exception e){}

        // update view with new sprites and moves
        battleView.setBotPokemonImageURL(battleMacro.getBattleMicro().getBotTeam().get(0).getBotImage());
        battleView.setPlayerPokemonImageURL(battleMacro.getBattleMicro().getUserTeam().get(0).getPlayerImage());

        battleView.setCurrPokemon(battleMacro.getBattleMicro().getUserTeam().get(0)); // update curr poke
        battleView.setBotCurrPokemon(battleMacro.getBattleMicro().getBotTeam().get(0)); // update curr bot poke
        battleView.updatePokemonSprites(); // update sprites and names shown
        battleView.updateMovesBox(); // update 4moves box with curr pokemon move names

        // update both pokemon's healthbar
        double newPercentageHealthPlayer = battleMacro.getBattleMicro().getUser().getCurrPokemon().getHP() / battleMacro.getBattleMicro().getUser().getCurrPokemon().getMaxHp();
        double newPercentageHealthBot = battleMacro.getBattleMicro().getBotTeam().get(0).getHP() / battleMacro.getBattleMicro().getBotTeam().get(0).getMaxHp();
        double newHealthWidthPlayer = (newPercentageHealthPlayer * battleView.getNAMETEXTBARWIDTH());
        double newHealthWidthBot = (newPercentageHealthBot * battleView.getNAMETEXTBARWIDTH());

        battleView.getPlayerHpBar().setWidth(newHealthWidthPlayer);
        battleView.getBotHpBar().setWidth(newHealthWidthBot);

    }

}
