# Gerenciamento de Usuários com Rastreamento Facial

## Descrição

Aplicativo Android para gerenciamento de usuários (CRUD) com captura de foto utilizando rastreamento facial em tempo real. Durante a pré-visualização da câmera, o aplicativo fornece feedback visual sobre centralização e distância do rosto, utilizando aprendizado de máquina no dispositivo.

## Tecnologias Utilizadas

- Kotlin
- Jetpack Compose (interface)
- Room (banco de dados SQLite)
- CameraX (integração com a câmera)
- ML Kit Face Detection (rastreamento facial)
- Coil (carregamento de imagens)
- KSP (Kotlin Symbol Processing)

## Pré‑requisitos

- Android Studio
- Dispositivo Android ou emulador com suporte a câmera (recomenda‑se dispositivo físico)
- Depuração USB ativada no dispositivo (ou depuração via Wi‑Fi)
- SDK mínimo: 24 (Android 7.0)
- SDK alvo: 34

## Configuração do Projeto

### 1. Clonar o repositório

**Importante:** Clone em um diretório **sem espaços** na unidade **C:** (exemplo: `C:\AndroidProjects\UserManagement`). Clonar em outras unidades (ex: `B:`) pode causar erros de permissão ou problemas com o FileProvider.
git clone https://github.com/seu-usuario/user-management-eyetec.git

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
