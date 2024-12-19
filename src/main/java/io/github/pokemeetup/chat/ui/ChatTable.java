package io.github.pokemeetup.chat.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import io.github.pokemeetup.chat.model.ChatMessage;
import io.github.pokemeetup.chat.service.ChatService;
import lombok.Getter;

public class ChatTable extends Table {

    private final ChatService chatService;
    private final Skin skin;
    private final ScrollPane messageScroll;
    private final Table messageTable;
    private final TextField inputField;

    @Getter
    private boolean active;

    public ChatTable(Skin skin, ChatService chatService) {
        super(skin);
        this.skin = skin;
        this.chatService = chatService;

        setFillParent(false);
        pad(10);

        messageTable = new Table(skin);
        messageTable.top().left();
        messageScroll = new ScrollPane(messageTable, skin);
        messageScroll.setFadeScrollBars(false);
        messageScroll.setScrollingDisabled(true, false);

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        inputField = new TextField("", textFieldStyle);
        inputField.setMessageText("Press T to chat...");
        inputField.setFocusTraversal(false);


        inputField.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (!active) return false;
                if (keycode == Input.Keys.ENTER) {
                    String content = inputField.getText().trim();
                    if (!content.isEmpty()) {
                        chatService.sendMessage(content);
                        inputField.setText("");
                    }
                    deactivate();
                    return true;
                } else if (keycode == Input.Keys.ESCAPE) {
                    deactivate();
                    return true;
                } else if (keycode == Input.Keys.UP) {

                    String previousMessage = chatService.getPreviousHistoryMessage(inputField.getText());
                    inputField.setText(previousMessage);
                    inputField.setCursorPosition(previousMessage.length());
                    return true;
                } else if (keycode == Input.Keys.DOWN) {

                    String nextMessage = chatService.getNextHistoryMessage();
                    inputField.setText(nextMessage);
                    inputField.setCursorPosition(nextMessage.length());
                    return true;
                }
                return false;
            }
        });

        row().expand().fill().padBottom(5);
        add(messageScroll).expand().fill().row();

        add(inputField).expandX().fillX().height(30);

        updateMessages();
    }

    public void activate() {
        active = true;
        chatService.activateChat();
        inputField.setVisible(true);

        Gdx.app.postRunnable(() -> {
            Stage stage = getStage();
            if (stage != null) {
                stage.setKeyboardFocus(inputField);
            }
        });
    }

    public void deactivate() {
        active = false;
        chatService.deactivateChat();
        inputField.setText("");
        Stage stage = getStage();
        if (stage != null) {
            stage.unfocus(inputField);
        }
    }

    public void updateMessages() {
        messageTable.clear();
        for (ChatMessage msg : chatService.getMessages()) {
            Label nameLabel = new Label(msg.getSender()+": ", skin);
            Label contentLabel = new Label(msg.getContent(), skin);
            contentLabel.setWrap(true);

            Table msgTable = new Table(skin);
            msgTable.left().top();
            msgTable.add(nameLabel).padRight(5);
            msgTable.add(contentLabel).expandX().fillX();

            messageTable.add(msgTable).expandX().fillX().padBottom(2).row();
        }

        messageScroll.layout();
        messageScroll.scrollTo(0,0,0,0);
    }
}
