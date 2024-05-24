#!/bin/bash

# Функция для сбора файлов main.tex
collect_main_files() {
    for file in "$1"/*; do
        if [ -d "$file" ]; then
            # Рекурсивно вызываем функцию для каждой поддиректории
            collect_main_files "$file"
        elif [[ $file == *main.tex ]]; then
            # Путь к файлу main.tex
            main_file="$file"

            # Переходим в директорию, содержащую main.tex
            dir_path=$(dirname "$main_file")
            cd "$dir_path" || exit

            rm "main.pdf"
            rm -f "main.bbl"
            rm -f "main.blg"

            echo "Собираем $dir_path"

            # Вызываем команду сборки LaTeX с xelatex и опцией -shell-escape
            xelatex -shell-escape -interaction=nonstopmode "$main_file"

            # Вызываем команду bibtex для обработки библиографии
            bibtex "main"

            # Повторно вызываем команду сборки LaTeX для включения списка литературы
            xelatex -shell-escape -interaction=nonstopmode "$main_file"
            xelatex -shell-escape -interaction=nonstopmode "$main_file"

            mv "main.pdf" "/Users/dsklifasovskiy/FA_Diploma"

            # Возвращаемся в исходную директорию
            cd - || exit
        fi
    done
}

# Указываем директорию, в которой нужно собрать файлы main.tex
directory_to_collect="/Users/dsklifasovskiy/FA_Diploma/src"

# Вызываем функцию для сбора файлов main.tex
collect_main_files "$directory_to_collect"

# Вызываем скрипт для очистки
./clean.sh

echo "Сборка и очистка завершены."