package com.chickengram.ai;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

import xyz.nextalone.nagram.NaConfig;

public final class AiClient {

    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    public static final class Message {
        public final String role;
        public final String text;

        public Message(String role, String text) {
            this.role = role;
            this.text = text;
        }
    }

    private static final int FORMAT_OPENAI_CHAT = 0;
    private static final int FORMAT_OPENAI_RESPONSE = 1;
    private static final int FORMAT_ANTHROPIC = 2;
    private static final int FORMAT_CUSTOM = 3;

    private static final String[] PROVIDER_NAMES = {
        "OpenAI", "Gemini", "Groq", "DeepSeek", "xAI", "Zhipu AI", "Mistral", "OpenRouter", "Qwen", "Moonshot", "SiliconFlow", "Свой сервис"
    };
    private static final String[] PROVIDER_URLS = {
        "https://api.openai.com/v1",
        "https://generativelanguage.googleapis.com/v1beta/openai",
        "https://api.groq.com/openai/v1",
        "https://api.deepseek.com/v1",
        "https://api.x.ai/v1",
        "https://open.bigmodel.cn/api/paas/v4",
        "https://api.mistral.ai/v1",
        "https://openrouter.ai/api/v1",
        "https://dashscope.aliyuncs.com/compatible-mode/v1",
        "https://api.moonshot.cn/v1",
        "https://api.siliconflow.cn/v1"
    };
    private static final String[] PROVIDER_MODELS = {
        "gpt-4.1-mini",
        "gemini-2.5-flash",
        "llama-3.3-70b-versatile",
        "deepseek-chat",
        "grok-3-mini-fast",
        "GLM-4-Flash",
        "mistral-small-latest",
        "meta-llama/llama-3.3-70b-instruct",
        "qwen-turbo-latest",
        "moonshot-v1-8k",
        "Qwen/Qwen2.5-7B-Instruct"
    };

    private AiClient() {
    }

    public static String providerName() {
        final int provider = NaConfig.INSTANCE.getLlmProvider().Int();
        return provider >= 0 && provider < PROVIDER_NAMES.length ? PROVIDER_NAMES[provider] : PROVIDER_NAMES[PROVIDER_NAMES.length - 1];
    }

    public static boolean hasKey() {
        return !TextUtils.isEmpty(apiKey());
    }

    public static String model() {
        final int provider = NaConfig.INSTANCE.getLlmProvider().Int();
        String value;
        switch (provider) {
            case 0: value = NaConfig.INSTANCE.getLlmOpenAIModel().String(); break;
            case 1: value = NaConfig.INSTANCE.getLlmGeminiModel().String(); break;
            case 2: value = NaConfig.INSTANCE.getLlmGroqModel().String(); break;
            case 3: value = NaConfig.INSTANCE.getLlmDeepSeekModel().String(); break;
            case 4: value = NaConfig.INSTANCE.getLlmXAIModel().String(); break;
            case 5: value = NaConfig.INSTANCE.getLlmZhipuAIModel().String(); break;
            case 6: value = NaConfig.INSTANCE.getLlmMistralModel().String(); break;
            case 7: value = NaConfig.INSTANCE.getLlmOpenRouterModel().String(); break;
            case 8: value = NaConfig.INSTANCE.getLlmQwenModel().String(); break;
            case 9: value = NaConfig.INSTANCE.getLlmMoonshotModel().String(); break;
            case 10: value = NaConfig.INSTANCE.getLlmSiliconFlowModel().String(); break;
            default: value = NaConfig.INSTANCE.getLlmCustomModel().String(); break;
        }
        if (TextUtils.isEmpty(value)) {
            return provider >= 0 && provider < PROVIDER_MODELS.length ? PROVIDER_MODELS[provider] : "gpt-4.1-mini";
        }
        return value;
    }

    public static String complete(String systemPrompt, List<Message> messages) throws Exception {
        final String key = apiKey();
        if (TextUtils.isEmpty(key)) {
            throw new Exception("Не указан ключ API. Откройте «ИИ-сервис» и вставьте ключ выбранного сервиса.");
        }
        final int provider = NaConfig.INSTANCE.getLlmProvider().Int();
        final boolean preset = provider >= 0 && provider < PROVIDER_URLS.length;
        final int format = preset ? FORMAT_OPENAI_CHAT : NaConfig.INSTANCE.getLlmApiFormat().Int();
        String url = preset ? PROVIDER_URLS[provider] : NaConfig.INSTANCE.getLlmApiUrl().String();
        if (TextUtils.isEmpty(url)) {
            url = PROVIDER_URLS[0];
        }
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        if (format != FORMAT_CUSTOM) {
            url = stripSuffix(stripSuffix(stripSuffix(url, "/chat/completions"), "/messages"), "/responses");
        }
        final String model = model();
        final double temperature = temperature();
        switch (format) {
            case FORMAT_OPENAI_RESPONSE:
                return parseResponses(post(url + "/responses", bearer(key), responsesBody(model, systemPrompt, messages, temperature)));
            case FORMAT_ANTHROPIC:
                return parseAnthropic(post(url + "/messages", anthropicHeaders(key), anthropicBody(model, systemPrompt, messages)));
            case FORMAT_CUSTOM:
                return parseChat(post(url, bearer(key), chatBody(model, systemPrompt, messages, temperature)));
            default:
                return parseChat(post(url + "/chat/completions", bearer(key), chatBody(model, systemPrompt, messages, temperature)));
        }
    }

    private static String apiKey() {
        final String keys = NaConfig.INSTANCE.getLlmApiKeys().String();
        if (keys == null) {
            return null;
        }
        for (String key : keys.split(",")) {
            if (!key.trim().isEmpty()) {
                return key.trim();
            }
        }
        return null;
    }

    private static double temperature() {
        try {
            final double value = Double.parseDouble(NaConfig.INSTANCE.getLlmTemperature().String());
            return Double.isNaN(value) ? 0.7 : Math.max(0, Math.min(2, value));
        } catch (Exception e) {
            return 0.7;
        }
    }

    private static String stripSuffix(String value, String suffix) {
        return value.endsWith(suffix) ? value.substring(0, value.length() - suffix.length()) : value;
    }

    private static String[][] bearer(String key) {
        return new String[][] { { "Authorization", "Bearer " + key } };
    }

    private static String[][] anthropicHeaders(String key) {
        return new String[][] { { "x-api-key", key }, { "anthropic-version", "2023-06-01" } };
    }

    private static JSONObject chatBody(String model, String systemPrompt, List<Message> messages, double temperature) throws Exception {
        final JSONArray array = new JSONArray();
        if (!TextUtils.isEmpty(systemPrompt)) {
            array.put(new JSONObject().put("role", "system").put("content", systemPrompt));
        }
        for (Message message : messages) {
            array.put(new JSONObject().put("role", message.role).put("content", message.text));
        }
        return new JSONObject().put("model", model).put("messages", array).put("temperature", temperature);
    }

    private static JSONObject responsesBody(String model, String systemPrompt, List<Message> messages, double temperature) throws Exception {
        final JSONArray array = new JSONArray();
        for (Message message : messages) {
            array.put(new JSONObject().put("role", message.role).put("content", message.text));
        }
        final JSONObject body = new JSONObject().put("model", model).put("input", array).put("temperature", temperature);
        if (!TextUtils.isEmpty(systemPrompt)) {
            body.put("instructions", systemPrompt);
        }
        return body;
    }

    private static JSONObject anthropicBody(String model, String systemPrompt, List<Message> messages) throws Exception {
        final JSONArray array = new JSONArray();
        for (Message message : messages) {
            array.put(new JSONObject().put("role", message.role).put("content", message.text));
        }
        final JSONObject body = new JSONObject().put("model", model).put("max_tokens", 4096).put("messages", array);
        if (!TextUtils.isEmpty(systemPrompt)) {
            body.put("system", systemPrompt);
        }
        return body;
    }

    private static String parseChat(JSONObject json) throws Exception {
        final JSONArray choices = json.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            throw new Exception("Сервис не прислал ответ.");
        }
        final JSONObject message = choices.getJSONObject(0).optJSONObject("message");
        final String content = message != null ? message.optString("content", "") : "";
        if (content.isEmpty()) {
            throw new Exception("Сервис прислал пустой ответ.");
        }
        return content.trim();
    }

    private static String parseResponses(JSONObject json) throws Exception {
        final JSONArray output = json.optJSONArray("output");
        if (output != null) {
            for (int i = 0; i < output.length(); i++) {
                final JSONObject item = output.getJSONObject(i);
                if (!"message".equals(item.optString("type"))) {
                    continue;
                }
                final JSONArray content = item.optJSONArray("content");
                if (content == null) {
                    continue;
                }
                for (int j = 0; j < content.length(); j++) {
                    final JSONObject block = content.getJSONObject(j);
                    if ("output_text".equals(block.optString("type"))) {
                        return block.optString("text", "").trim();
                    }
                }
            }
        }
        throw new Exception("Сервис не прислал ответ.");
    }

    private static String parseAnthropic(JSONObject json) throws Exception {
        final JSONArray content = json.optJSONArray("content");
        if (content != null) {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < content.length(); i++) {
                final JSONObject block = content.getJSONObject(i);
                if ("text".equals(block.optString("type"))) {
                    builder.append(block.optString("text", ""));
                }
            }
            if (builder.length() > 0) {
                return builder.toString().trim();
            }
        }
        throw new Exception("Сервис не прислал ответ.");
    }

    private static JSONObject post(String address, String[][] headers, JSONObject body) throws Exception {
        final HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        try {
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(180000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            for (String[] header : headers) {
                connection.setRequestProperty(header[0], header[1]);
            }
            try (OutputStream out = connection.getOutputStream()) {
                out.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }
            final int code = connection.getResponseCode();
            final InputStream stream = code >= 400 ? connection.getErrorStream() : connection.getInputStream();
            final String text = stream == null ? "" : read(stream);
            if (code >= 400) {
                throw new Exception(describeError(code, text));
            }
            return new JSONObject(text);
        } finally {
            connection.disconnect();
        }
    }

    private static String describeError(int code, String text) {
        String detail = null;
        try {
            final Object parsed = new org.json.JSONTokener(text).nextValue();
            JSONObject json = parsed instanceof JSONArray ? ((JSONArray) parsed).optJSONObject(0) : (JSONObject) parsed;
            if (json != null) {
                final JSONObject error = json.optJSONObject("error");
                detail = error != null ? error.optString("message", null) : json.optString("message", null);
            }
        } catch (Exception ignored) {
        }
        if (code == 401 || code == 403) {
            return "Ключ API не подошёл (ошибка " + code + "). Проверьте ключ в «ИИ-сервисе».";
        }
        if (code == 429) {
            return "Слишком много запросов или закончился лимит (ошибка 429). Попробуйте позже.";
        }
        if (TextUtils.isEmpty(detail)) {
            detail = text.length() > 300 ? text.substring(0, 300) : text;
        }
        return "Ошибка " + code + ": " + detail;
    }

    private static String read(InputStream stream) throws Exception {
        try (InputStream in = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toString("UTF-8");
        }
    }
}
