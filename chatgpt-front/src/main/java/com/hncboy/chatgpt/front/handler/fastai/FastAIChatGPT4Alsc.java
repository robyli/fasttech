package com.hncboy.chatgpt.front.handler.fastai;

import cn.hutool.core.util.StrUtil;
import com.hncboy.chatgpt.base.config.ChatConfig;
import com.hncboy.chatgpt.base.domain.entity.ChatMessageDO;
import com.hncboy.chatgpt.base.enums.ApiTypeEnum;
import com.hncboy.chatgpt.base.util.OkHttpClientUtil;
import com.hncboy.chatgpt.front.domain.request.ChatProcessRequest;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatChoice;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.entity.chat.Message;
import jakarta.annotation.Resource;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FastAIChatGPT4Alsc {

    @Resource
    private ChatConfig chatConfig;

    public Object chat(ChatProcessRequest request){

        String alscToken = request.getAlscToken();
        if(alscToken == null || !alscToken.equals("ALSC-SRE")){
            return null;
        }
        ChatMessageDO chatMessageDO = new ChatMessageDO();
        chatMessageDO.setApiType(ApiTypeEnum.API_KEY);
        chatMessageDO.setApiKey(chatConfig.getOpenaiApiKey());
        chatMessageDO.setUserId(100860);
        chatMessageDO.setContent(request.getPrompt());
        chatMessageDO.setModelName(chatConfig.getOpenaiApiModel());
        chatMessageDO.setParentMessageId(null);

        LinkedList<Message> messages = new LinkedList<>();
        messages.addFirst(Message.builder().role(Message.Role.USER)
                .content(chatMessageDO.getContent())
                .build());
        if (StrUtil.isNotBlank(request.getSystemMessage())) {
            // 系统消息
            Message systemMessage = Message.builder()
                    .role(Message.Role.SYSTEM)
                    .content(request.getSystemMessage())
                    .build();
            messages.addFirst(systemMessage);
        }
        ChatCompletion chatCompletion = ChatCompletion.builder()
                .model(chatConfig.getOpenaiApiModel())
                .temperature(0.8)
                .topP(1.0)
                .n(1)
                .presencePenalty(1)
                .messages(messages)
                .build();

        OkHttpClient okHttpClient = OkHttpClientUtil.getInstance(ApiTypeEnum.API_KEY, chatConfig.getTimeoutMs(),
                chatConfig.getTimeoutMs(), chatConfig.getTimeoutMs(), null);
        ChatCompletionResponse response = OpenAiClient.builder()
                .okHttpClient(okHttpClient)
                .apiKey(Collections.singletonList(chatConfig.getOpenaiApiKey()))
                .apiHost(chatConfig.getOpenaiApiBaseUrl())
                .build()
                .chatCompletion(chatCompletion);

        List<ChatChoice> choices = response==null?null:response.getChoices();
        ChatChoice choice = choices==null?null:choices.get(0);
        Message message = choice==null?null:choice.getMessage();
        String content = message==null?"":message.getContent();
        Map<String,Object> result = new HashMap<>();
        result.put("reply",content);
        result.put("detail",response);
        return result;
    }
}
