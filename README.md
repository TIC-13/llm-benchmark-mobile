<div align="center">

# Speed.AI - LLM Edition

**Easy benchmarking of LLMs using MLC LLM engine**

</div>

<div align="center">
  <img src="https://github.com/user-attachments/assets/af0417b0-c867-459d-ac03-d9e9a7653630" alt="Benchmarking Screen" width="25%"/>
  <img src="https://github.com/user-attachments/assets/93a0523c-557d-40d0-8567-0f68f65cd853" alt="Performance Results" width="25%"/>
</div>

## About

<b>Speed.AI - LLM Edition</b> is an Android app designed to benchmark Large Language Models (LLMs). It measures key performance indicators such as tokens per second and CPU/GPU/RAM usage. The benchmarks are conducted using a standardized dataset for all models, ensuring consistent input conditions. Additionally, the app allows for custom conversations with any supported model, providing benchmarking results afterward.

## Get Started


This app is built on the MLC LLM Android app. Therefore, the instructions and steps for building and running the app are identical to those of the MLC LLM app. To get started, follow this [documentation](https://llm.mlc.ai/docs/).

If you wish to add new models to the benchmarking, add them the same way as in MLC.

## About the ranking

This app sends benchmarking results to a ranking system, where you can view the benchmarking results by each phone that has run it.

The ranking is shared with the [Speed.AI - AI Benchmarking]([https://github.com/TIC-13/llm-benchmark-mobile/tree/dev](https://github.com/TIC-13/benchmarking-ai-v2)) app.
If you want to host an instance of the ranking, see these repositories: [Front](https://github.com/TIC-13/benchmark-ranking-front) | [Back](https://github.com/TIC-13/benchmark-ranking-back).

Not hosting the ranking has no impact on the app’s functionality—you can still build and use the app normally.

## Environment

In the Android project, set the backend address in `local.properties` under `API_ADDRESS` and assign a base64-encoded 32-byte (AES-256) value to `API_KEY`. Ensure that this key matches the one used in the backend.

Additionally, in `local.properties`, set `RANKING_ADDRESS` to the address where the ranking is hosted.  


