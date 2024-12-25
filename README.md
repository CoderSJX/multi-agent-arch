# Multi-Agent Architecture for Unified Interaction Entry

[![GitHub](https://img.shields.io/github/stars/CoderSJX/multi-agent-arch?style=social)](https://github.com/CoderSJX/multi-agent-arch)
[![GitHub license](https://img.shields.io/github/license/CoderSJX/multi-agent-arch)](https://github.com/CoderSJX/multi-agent-arch/blob/main/LICENSE)

## 项目背景

2025年被广泛认为是智能体（Agent）的元年，随着智能技术的发展，越来越多的企业和个人尝试构建适应自身业务需求的智能体。然而，智能体之间的切换和统一管理成为了一个新的挑战。用户希望仅通过一个交互入口就能调用多种类型的智能体，而不是为每个智能体设置独立的入口。本项目旨在解决这一难题，提供一个多智能体的统一入口解决方案。

## 项目需求

实现多智能体的统一入口，使用户能够通过单一交互入口与多个不同业务逻辑的智能体进行交互。

## 需求分析

为了满足上述需求，项目设计了智能体路由机制，基于大模型解析用户的意图，并根据解析结果将请求分发到相应的智能体上。此外，考虑到连续对话的需求，系统会基于用户最近五条提问的历史记录来识别用户的最新意图，以确保对话的连贯性和准确性。

### 技术栈

本项目采用Spring Boot、Spring-AI、Spring WebFlux、Redis、Qwen以及PostgreSQL等技术，构建了一个高性能、可扩展的多智能体服务平台。
## 实现设计

### 架构概述

1. **统一接口**：处理所有用户的请求，接收当前问题、用户token及额外参数。
2. **过滤器**：利用token建立用户上下文，便于后续处理中获取用户身份信息。
3. **意图识别**：存储用户提问至数据库，提取历史提问构成对话历史，交由专门的意图识别智能体解析。
4. **智能体匹配**：依据意图分类在数据库或内存中查找对应的智能体。
5. **智能体执行**：配置并调用智能体，处理业务逻辑。
6. **FunctionCall处理**：设计functioncall远程调用中心，集中处理所有functioncall，支持框架与业务分离。
7. **MessageList管理**：更新用户的MessageList，控制其长度，保证对话的连贯性。
8. **分布式支持**：考虑负载均衡和微服务架构，对智能体实例的存取进行改造，支持分布式部署。

### 小细节

- 使用Spring AI的OpenAI模型库，支持调用符合OpenAI格式的大模型。
- Agent实例默认使用内存存储（HashMap），可替换为ConcurrentHashMap或Redis。
- 默认采用流式调用和返回，已适配streaming functioncall。

## 项目代码

本项目的代码托管在GitHub上，提供了基本的代码框架，开发者可根据自己的业务需求进行扩展和调整。

- [GitHub仓库链接](https://github.com/CoderSJX/multi-agent-arch)

---

欢迎贡献代码，提出建议，一起完善这个项目！如果你有任何问题或需要帮助，请随时提交Issue或Pull Request。


### 数据库预置数据

```sql
INSERT INTO public.agents (id, description, enterprise_id, function_tools, is_public, system_prompt, temperature, type) VALUES (1, '意图识别智能体', null, e'[ {
            "type": "function",
            "function": {
                "description": "输出用户意图分类",
                "name": "print_intent",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "businessType": {
                            "type": "string",
                            "description": "业务分类"
                        }
                    }
                }
            }
        }]', null, '您是一名善于从历史提问中分析用户的最新意图的助手，请根据提问历史记录，分析并总结用户的最新问题的完整意图。你需要分析用户的意图之后，调用print_intent的方法，输出用户的意图分类，输出英文标识。用户的意图分类有 待办(todo)、问答(question)、日程(calender)、联系人(contact)', null, 'INTENT_DISPATCH');
INSERT INTO public.agents (id, description, enterprise_id, function_tools, is_public, system_prompt, temperature, type) VALUES (5, '待办智能体', null, e'[
        {
            "type": "function",
            "function": {
                "description": "执行待办命令",
                "name": "handleCommand",
                "parameters": {
                    "type": "object",
                    "properties": {
                        
                        "classification": {
                            "type": "string",
                            "description": "待办分类"
                        },
                        "operation": {
                            "type": "string",
                            "description": "操作"
                        }

              
                    }
                }
            }
        }
    ]', null, e'## 身份
你是待办小助手，你擅长分析用户的输入，将用户输入转化成一个操作指令。
## 要求
你必须按照流程处理待办。
你只能够处理待办，其他任何和待办无关的要求，你都处理不了，包括一些普通的对话，例如：你是谁，你会做什么。
## 认知
待办是一个记录用户待处理事项和已处理事项的模块。
待办有以下几个参数：
1. 待办的分类名。
2. 待办的发起人。
3. 待办的发起日期。
4. 待办的内容。

## 流程
1. 分析用户的输入，识别用户想要进行的操作，用户想要进行的操作有四种：modify（修改）、query（查询、查看）、add（追加内容）。默认是查看。
2. 调用handleCommand方法。', null, 'todo');
INSERT INTO public.agents (id, description, enterprise_id, function_tools, is_public, system_prompt, temperature, type) VALUES (3, '企业知识智能体', null, e'[{"type": "function",
            "function": {
                "description": "获取企业知识参考",
                "name": "get_knowledge",
                "parameters": {
                    "type": "object",
                    "properties": {
                        "question": {
                            "type": "string",
                            "description": "用户的具体问题"
                        }
                    }
                }
            }
        }]', null, e'## 身份
你是AI助理。
## 要求
语气正式、专业，态度友好，言语简洁干练，使用中文回答。
如果用户的提问，你无法理解或者无法给出准确的回复，那就调用企业知识查询来获取参考。

## 技能
你的技能列表如下：
1. 帮助用户处理待办。
2. 利用知识库查询，给用户解答企业知识。
3. 帮助用户处理日程。
4. 查询企业联系人。

```

## 