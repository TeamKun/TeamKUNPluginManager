<h1 align="center">TeamKUNPluginManager</h1>

<p align="center">
    <img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/TeamKun/TeamKUNPluginManager/Java%20CI%20with%20Maven?style=flat-square">
    <a href="https://www.codacy.com/gh/TeamKun/TeamKUNPluginManager/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=TeamKun/TeamKUNPluginManager&amp;utm_campaign=Badge_Grade">
        <img alt="Codacy grade" src="https://img.shields.io/codacy/grade/de19f8162c394e46b56db749a35df467?logo=codacy&style=flat-square">
    </a>
    <img alt="GitHub" src="https://img.shields.io/github/license/TeamKun/TeamKunPluginManager?style=flat-square">
    <img alt="Java version" src="https://img.shields.io/static/v1?label=Java%20version&message=1.8&color=success&style=flat-square">
    <a href="https://github.com.TeamKun/TeamKunPluginManager/wiki">
        <img alt="Docs" src="https://img.shields.io/static/v1?label=Docs&message=wiki&color=green&style=flat-square">
    </a>
    <br>
    プラグインをリモートからインストールします。
</p>

## 概要

+ TeamKUNPluginManagerは、[PlugMan](https://dev.bukkit.org/projects/plugman), [Installer](https://dev.bukkit.org/projects/plugin-installer)に並ぶ新しいプラグインマネージャです。
+ GitHubと連携し、リモートリポジトリのリリースからインストールを行うことが可能です。
+ 依存関係の解決機能を有しており`plugin.yml`に記述された依存関係がプラグインインストール時に自動でインストールされます。
+ プラグイン名の別名を自分で作成することができ、リモートで同期することが可能です。
+ [apt](https://salsa.debian.org/apt-team/apt)と[npm](https://github.com/npm/cli)を足して2で割ったようなコマンド操作感で、初心者でも簡単に扱うことが可能です。
+ SQLiteを用いて依存関係ツリーを構築します。

## 導入方法&ドキュメント
+ [こちら](https://github.com/TeamKun/TeamKunPluginManager/wiki)からご覧ください。
