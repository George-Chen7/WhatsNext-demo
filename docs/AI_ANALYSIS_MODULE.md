# AI Career Analysis Module

## Module Position

This module is the AI enhanced career planning analysis module. It combines Android local data, assessment results, prompt engineering, and a replaceable large model service layer.

Current phase uses `MockAiCareerService` so the course demo can run offline and remain stable. Phase 2 can replace it with `RemoteAiCareerService`.

## Data Flow

```text
Activity
    -> AiAnalysisViewModel
    -> AiCareerRepository
    -> AiCareerService
        -> MockAiCareerService
        -> RemoteAiCareerService
    -> AiAnalysisResponse
    -> AiReportActivity
```

Input data is wrapped by `AiAnalysisRequest`:

- `UserProfile`: nickname, age, gender, education, major, school, grade, expected industry, strengths, hobbies, notes
- `MbtiResult`: MBTI type and dimension scores
- `HollandResult`: RIASEC top code and dimension scores
- supplement notes: postgraduate plan, overseas plan, stability preference, and other user notes

Output data is `AiAnalysisResponse`:

- summary
- personalityStrengths
- suitableIndustries
- suitablePositions
- learningSuggestions
- actionPlan
- risks
- finalAdvice

## Prompt Engineering

`AiPromptBuilder` builds a structured Chinese prompt and asks the model to act as a career planning consultant.

The prompt requires:

- objective and specific analysis for college students
- combined use of user profile, MBTI, and Holland results
- no absolute conclusion
- MBTI must not be treated as the only decision basis
- fixed JSON response format

## Real API Reserved Position

`RemoteAiCareerService` is the reserved real API implementation.

Local demo key storage:

- Copy `local.properties.example` to `local.properties`.
- Fill `AI_API_KEY` and `AI_API_ENDPOINT` in `local.properties`, or set environment variables with the same names.
- `local.properties` is ignored by Git, so real keys are not committed.
- Gradle injects these values into `BuildConfig` at build time for local testing.

Phase 2 TODO:

- Use Retrofit or OkHttp to call OpenAI, DeepSeek, Qwen, or another model provider from `RemoteAiCareerService`.
- Parse the provider response content into `AiAnalysisResponse`.
- For production apps, move model calls behind a backend proxy or short lived token API. Do not ship long lived API keys inside APKs.
- Do not commit a real API key to Git.

## Course Design Highlights

This module demonstrates:

- network interface reservation
- local data integration
- prompt engineering
- large model analysis capability
- fusion of assessment results and user profile
- MVVM structure with traditional Android XML UI
- Room report persistence and card based report display
