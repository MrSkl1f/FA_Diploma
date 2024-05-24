#!/bin/bash

cleanup_build_files() {
    for file in "$1"/*; do
        if [ -d "$file" ]; then
            # Рекурсивно вызываем функцию для каждой поддиректории
            cleanup_build_files "$file"
        elif [[ $file == *.aux || $file == *.log || $file == *.out || $file == *.toc || $file == *.fdb_latexmk || $file == *.fls || $file == *.bbl || $file == *.blg || $file == *.aux || $file == *.bbl ]]; then
            # Удаляем файлы с расширениями .aux, .log, .out и .toc
            rm "$file"
        fi
    done
}

# Указываем абсолютный путь к директории, которую нужно очистить
directory_to_clean="/Users/dsklifasovskiy/FA_Diploma/src"

# Вызываем функцию очистки файлов билда
cleanup_build_files "$directory_to_clean"
echo "Очистка файлов билда завершена."