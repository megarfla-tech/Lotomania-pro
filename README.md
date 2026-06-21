# Lotomania AI PRO 🪐

**Plataforma de Telemetria Estatística, Decodificação Espacial e Inteligência Artificial para Lotomania.**

---

O **Lotomania AI PRO** é um aplicativo Android nativo premium, desenvolvido sob os mais rigorosos padrões da engenharia de software moderna (SOLID, Clean Architecture, MVVM), inspirado em interfaces analíticas financeiras de alta densidade como o Bloomberg Terminal e o TradingView.

O app visa decodificar e analisar o histórico total dos sorteios da Lotomania, identificar anomalias estatísticas, gerar mapas de calor em tempo real, simular apostas retroativas via backtesting financeiro e compilar previsões heurísticas integradas à inteligência generativa do Google Gemini.

---

## 🚀 Principais Módulos do Sistema

### 📊 1. Geral & Telemetria (Dashboard)
- **Status do Sistema**: Monitoramento em tempo real do ecossistema de dados.
- **Sincronização Online**: Atualização em segundo plano dos resultados de concursos diretamente de end-points oficiais da CEF (com fallback dinâmico local de semente).
- **Cartões de Telemetria**: Métricas-chave como total de concursos catalogados, estabilidade geral dos desvios padrões e índice de volatilidade.

### 📈 2. Estatísticas Avançadas (Interactive Canvas)
- **Interactive Heatmap**: Grade interativa total de 00 a 99 permitindo tocar individualmente nas dezenas para extrair atrasos relativos e frequência absoluta.
- **Decade Distribution Bar Chart**: Gráfico dinâmico desenhado via Canvas segmentando as dezenas em grupos de 10.
- **Curva de Distribuição Normal (Gauss)**: Modelos de densidade avaliando a ocorrência e dispersão das dezenas por Linhas (0-9) e Colunas (finais de 0-9).
- **Quadrant Summary Grid**: Divisão em 4 quadrantes clássicos do volante oficial da Lotomania, computando o fechamento de dezenas.

### 🧠 3. IA PRO (Google Gemini Core)
- **Núcleo de Processamento Neural**: Integração nativa de baixo overhead com o modelo `gemini-3.5-flash` utilizando instruções contextuais de sistema configuradas em português brasileiro.
- **Relatório Diagnóstico**: Síntese explicada contendo detecção de tendências recentes, desvio de paridade e alertas de anomalias estatísticas reais detectadas no histórico.

### 🎰 4. Sistemas (Gerador, Simulador, Comparador)

#### A. Gerador Inteligente
Geração paramétrica de **1 a 100 bilhetes** contendo 50 dezenas, impulsionado por 11 algoritmos matemáticos:
- *Aleatório Clássico, Estatístico Prioritário, IA Preditiva (Weights Random Forest/LSTM), Conservador, Agressivo (Quentes + Frios Atrasados), Equilibrado (Paridades 25P/25I), Tendências Recentes, Estritamente Frios, Estritamente Quentes, Misto 50/50 e Anti-Repetição (Bane o último concurso).*

#### B. Simulador de Investimentos (Backtest)
- **Avaliação Retroativa**: Executa simulações retroativas testando jogos criados pelo usuário ou dezenas manuais contra as últimas 10, 50, 100 ou 500 rodadas históricas da Lotomania.
- **Análise de Margem**: Calcula o custo total investido (R$ 3,00 por aposta), ganho em premiações nas dezenas de corte oficial (20, 19, 18, 17, 16, 15 ou 0 acertos), lucro/prejuízo líquido real e taxa máxima de estabilidade.

#### C. Comparador Multiciclo
- Mapeia interseção (sobreposição matemática), dispersão espacial de coeficientes e coincidências de dezenas repetidas através de agrupamentos configuráveis.

---

## 🏗️ Arquitetura de Software

O ecossistema foi estruturado com base nos preceitos de robustez, testabilidade local e alta performance:
- **Presentation Layer**: UI responsiva edge-to-edge programada em **Jetpack Compose** sob as diretrizes do Material Design 3.
- **State Management**: Fluxo unidirecional reativo gerenciado por **ViewModel** acompanhado de `StateFlow` e `MutableStateFlow` nativos.
- **Local Persistence Layer**: **Room Database** mapeando entidades, conversores customizados (`TypeConverters`) e rotinas estáveis de inserção transacional rápida.
- **Remote Integration**: Abstração direta resiliente via **OkHttpClient** para interagir com o modelo Gemini e baixar resultados atualizados.

---

## 🛠️ Guia de Compilação & Execução Local

### Requisitos Prévios
- JDK 17 (Zulú ou similar) instalado.
- Android SDK nível de API 34 ou superior.
- Gradle catalogado.

### Passos Rápidos
1. Navegue até o diretório raiz do projeto.
2. Crie e preencha suas variáveis no arquivo `.env` (use o `.env.example` de referência):
   ```properties
   GEMINI_API_KEY="SUA_CHAVE_AQUI"
   ```
3. Execute o comando de compilação via CLI:
   ```bash
   gradle assembleDebug
   ```
4. O arquivo APK empacotado estará disponível em:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 🧪 Estratégia de Integração Contínua (CI)

O Lotomania AI PRO conta com integração através do GitHub Actions (`.github/workflows/android.yml`). Toda ação de push ou pull request na branch `main` executa as seguintes etapas:
- Resolução e validação da integridade de pacotes do Gradle wrapper.
- Preparação e injeção do ambiente e JDK 17.
- Compilação do build variant debug de forma resiliente.
- Upload automático do binário `.apk` compilado para download na área de artefatos da execução do Github!

---

## ⚠️ Isenção de Responsabilidade Estatística

> **Aviso Legal Obrigatório**: O Lotomania AI PRO é um software acadêmico e analítico de telemetria estatística. O aplicativo realiza cálculos rigorosos baseados na matemática combinatória de probabilidade de Bernoulli, desvio quadrático médio e redes inteligentes computacionais. Devido ao caráter puramente randômico e equiprovável dos sorteios de loteria, nenhuma técnica analítica ou generativa de IA é capaz de garantir lucros ou prever dezenas exatas. Jogue com moderação e responsabilidade financeira.
