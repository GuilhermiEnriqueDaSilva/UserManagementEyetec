# Gerenciamento de Usuários com Rastreamento Facial

## Descrição

Aplicativo Android para gerenciamento de usuários (CRUD) com captura de foto utilizando rastreamento facial em tempo real. Durante a pré-visualização da câmera, o aplicativo fornece feedback visual sobre centralização e distância do rosto, utilizando aprendizado de máquina no dispositivo.

## Tecnologias Utilizadas

## Tecnologias Utilizadas

- **Kotlin** – linguagem moderna e concisa para desenvolvimento Android, com suporte a corrotinas e interoperabilidade com Java.
- **Jetpack Compose** – framework declarativo para construção de interfaces, permitindo UI reativa e com menos código boilerplate.
- **Room** – biblioteca de abstração sobre SQLite, facilitando a persistência de dados com segurança de tipo e suporte a Flow.
- **CameraX** – API de câmera lifecycle-aware que simplifica a captura de imagens e integração com o ciclo de vida do Android.
- **ML Kit Face Detection** – SDK de aprendizado de máquina on-device da Google, que permite detecção facial em tempo real sem necessidade de conexão com a internet.
- **Coil** – biblioteca leve para carregamento de imagens com suporte a cache e async, ideal para exibir fotos do usuário.
- **KSP (Kotlin Symbol Processing)** – processador de anotações utilizado pelo Room para gerar código em tempo de compilação.

## Por que essas tecnologias?

| Tecnologia | Justificativa |
|------------|----------------|
| **Kotlin** | Linguagem oficial para Android; mais segura e expressiva que Java. |
| **Compose** | UI moderna, reativa e recomendada pela Google para novos projetos; reduz a complexidade do XML. |
| **Room** | Abstração segura do SQLite; integração nativa com corrotinas e Flow. |
| **CameraX** | Gerencia permissões, ciclo de vida e rotação automaticamente; reduz o código necessário para captura. |
| **ML Kit** | Detecção facial offline e em tempo real; fácil integração com CameraX. |
| **Coil** | Mais leve que Picasso/Glide, suporte nativo a Compose e corrotinas. |
| **KSP** | Alternativa mais rápida e compatível ao KAPT, usada pelo Room. |

## Pré‑requisitos

- Android Studio
- Dispositivo Android ou emulador com suporte a câmera (recomenda‑se dispositivo físico)
- Depuração USB ativada no dispositivo (ou depuração via Wi‑Fi)
- SDK mínimo: 24 (Android 7.0)
- SDK alvo: 34

## Configuração do Projeto

### 1. Clonar o repositório

**Importante:** Clone em um diretório **sem espaços** na unidade **C:** (exemplo: `C:\AndroidProjects\UserManagement`). Clonar em outras unidades (ex: `B:`) pode causar erros de permissão ou problemas com o FileProvider.

### 2. Abrir no Android Studio

- Abra o Android Studio → `File` → `Open`
- Selecione a pasta do projeto clonado
- Aguarde a sincronização do Gradle

### 3. Dependências

Todas as dependências são gerenciadas via Version Catalog (`libs.versions.toml`). Nenhuma alteração manual é necessária. A primeira sincronização exigirá conexão com a internet.

### 4. Executar o aplicativo

- Conecte seu dispositivo Android (com depuração USB ativada) ou inicie um emulador com suporte a câmera.
- Selecione a configuração de execução `app` e clique em `Run` (triângulo verde).

## Banco de Dados

- Tipo: SQLite via Room
- Arquivo do banco: `user_database`
- Localização (armazenamento interno):  
  `/data/data/com.example.usermanagementeyetec/databases/user_database`
- Criação: automática na primeira execução (nenhuma migração manual é necessária)
- Estratégia de migração: `fallbackToDestructiveMigration()` – o banco é recriado se a versão do esquema mudar
  
## Funcionalidades

- **Listagem de usuários** – exibe nome, e‑mail e miniatura da foto
- **Criar usuário** – informar nome, e‑mail e capturar foto com a câmera frontal
- **Editar usuário** – alterar nome, e‑mail ou substituir a foto
- **Excluir usuário** – remove o usuário e a foto associada do armazenamento interno
- **Rastreamento facial em tempo real** – durante a pré‑visualização da câmera
  - Mensagens de feedback:
    - "Centralize o rosto" – rosto descentralizado horizontalmente
    - "Muito perto, afaste um pouco" – rosto ocupa grande parte da tela
    - "Muito longe, aproxime-se" – rosto muito pequeno
    - "Perfeito! Pode tirar a foto" – rosto bem posicionado

## Armazenamento de Imagens

- As fotos dos usuários são salvas no diretório interno privado do aplicativo:  
  `/data/data/com.example.usermanagementeyetec/files/images/user_{id}.jpg`
- Nenhuma permissão de armazenamento externo é necessária.
- Uma pasta temporária `files/temp/` é usada durante a captura da foto.

## Permissões

As seguintes permissões estão declaradas em `AndroidManifest.xml`:

- `CAMERA` – solicitada em tempo de execução quando o usuário toca em "Tirar foto com rastreio facial"
- `android.hardware.camera` – requisito de recurso de hardware

## Configuração do FileProvider

O arquivo `res/xml/file_paths.xml` define os caminhos acessíveis para compartilhar arquivos com a câmera:

```xml
<paths>
    <files-path name="files_root" path="." />
    <files-path name="images" path="images/" />
    <files-path name="temp" path="temp/" />
    <cache-path name="cache" path="." />
</paths>
```

## Instruções de Build e Execução
Utilize a variante de build app (debug).

A primeira compilação pode levar alguns minutos devido ao download das dependências.

Se ocorrer o erro Failed to find configured root, verifique se o projeto está localizado na unidade C: e se file_paths.xml contém a linha <files-path name="files_root" path="." />.

Se ocorrer Cannot obtain the package, execute Build → Clean Project e depois Build → Rebuild Project.

Limitações Conhecidas
Os limites de área do rosto (20000–80000 pixels) estão fixos no código e calibrados para smartphones comuns; podem precisar de ajuste para diferentes resoluções de tela.

A câmera usa exclusivamente a lente frontal; não há opção de alternar para a câmera traseira.

O projeto não inclui testes unitários ou instrumentados.


## Desafios e Soluções

### 1. Curva de aprendizado com Kotlin e Android Studio

- **Desafio:** Iniciei o projeto no IntelliJ IDEA, mas logo descobri que ele não tem suporte completo ao desenvolvimento Android. Precisei migrar para o **Android Studio**, o que demandou reconfigurar o ambiente e entender a nova estrutura de diretórios (`main`, `java`, `res`, etc.).
- **Desafio:** Meu conhecimento prévio era em Java – a sintaxe do Kotlin (classes data, `by`, `companion object`, `suspend fun`) gerou confusão inicial.
- **Solução:** Fiz pequenos projetos‑teste (CRUD simples, câmera básica) para praticar antes de implementar no projeto final. Consultei fóruns (Stack Overflow, Reddit) e analisei projetos open‑source para entender padrões de código.

### 2. Configuração de versões (Kotlin, AGP, KSP)

- **Desafio:** As versões do Kotlin, Android Gradle Plugin (AGP) e KSP precisavam ser compatíveis. O erro `Using kotlin.sourceSets DSL to add Kotlin sources is not allowed with built-in Kotlin` apareceu devido a incompatibilidades.
- **Solução:** Ajustei as versões no `libs.versions.toml` e adicionei `android.disallowKotlinSourceSets=false` no `gradle.properties` como solução temporária.

### 3. FileProvider e caminhos de arquivo

- **Desafio:** O erro `Failed to find configured root that contains /data/data/.../JPEG_...jpg` ocorria ao tentar usar a câmera. O FileProvider não reconhecia a raiz do diretório `files/`.
- **Solução:** Adicionei a linha `<files-path name="files_root" path="." />` no `file_paths.xml`, permitindo que o FileProvider acesse qualquer arquivo dentro do diretório `files/`.

### 4. Rastreamento facial com ML Kit

- **Desafio:** O código genérico `MlKitAnalyzer` que encontrei em referências estava desatualizado e com erros de tipo. O método `process()` do ML Kit mudou a assinatura.
- **Solução:** Implementei a detecção diretamente no `CameraScreen`, processando cada frame com `faceDetector.process(inputImage)` e atualizando o feedback na UI via State (Compose). Isso eliminou a camada de abstração problemática.

### 5. Projeto só roda no disco `C:`

- **Desafio:** Quando clonado em outra unidade (ex: `B:`), ocorriam erros de permissão e `Cannot obtain the package`.
- **Solução:** Migrei o projeto para `C:\AndroidProjects\UserManagement` (caminho sem espaços). Acredito que a causa seja a combinação de permissões de escrita restritas em unidades secundárias e o espaço em branco no nome da pasta original (`Projetos Programacao`). Recomendo sempre usar o disco `C:` e nomes sem espaços para projetos Gradle.

### 6. Dificuldade com a interface do Android Studio

- **Desafio:** A estrutura de pastas, o gerenciamento de builds e a sincronização do Gradle foram confusos inicialmente. Não sabia onde encontrar logs de erro ou como limpar o cache.
- **Solução:** Com o tempo e prática, aprendi a usar `Build` → `Clean Project`, `File` → `Sync Project with Gradle Files` e `File` → `Invalidate Caches and Restart` para resolver problemas recorrentes.
