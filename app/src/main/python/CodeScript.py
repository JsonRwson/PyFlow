import sys
from os.path import dirname, join
from com.chaquo.python import Python
import builtins

# function called by java app when run button is clicked
def main(CodeInputData, inputLines):

    # Get the directory for application-specific files to be stored in
    file_dir = str(Python.getPlatform().getApplication().getFilesDir())

    # Define the path for the output file where Python code output will be written to
    filename = join(dirname(file_dir), 'file.txt')

    try:
        # store reference to standard output stream
        original_stdout = sys.stdout

        # make code outputstream write to code output file
        sys.stdout = open(filename, 'w', encoding = 'utf8', errors = "ignore")

        # Define a custom input function to use the provided input lines
        input_index = 0
        def custom_input(prompt=""):

            nonlocal input_index

            # Write the prompt to stdout
            if prompt != "":
                sys.stdout.write(prompt+"\n")

            # Get the input data from the provided list of input lines
            if input_index < len(inputLines):
                input_data = inputLines[input_index]
                input_index += 1
                return input_data

            else:
                return ""

        # Replace the standard input function with the custom one which uses input data from the widget
        builtins.input = custom_input

        # Limit the number of lines that can be printed
        max_prints = 10000
        old_print = print

        def limited_print(*args, **kwargs):
            nonlocal max_prints
            if max_prints > 0:
                old_print(*args, **kwargs)
                max_prints -= 1

        # Replace the standard print function with the custom one which limits print size to prevent memory errors
        builtins.print = limited_print

        # execute the user written python code
        exec(CodeInputData)

    # catch exceptions and replace the output with the caught exception message
    except Exception as error:
        sys.stdout = original_stdout
        output = error

    finally:
        # close the output stream
        sys.stdout.close()

        # return the output stream to the standard
        sys.stdout = original_stdout

        # read the result of running the code from the file it outputted to
        output = open(filename, 'r').read()

    return str(output) # return code output
