## this command runs it
## gnuplot gnuplot.sh linear_data_train.csv
set datafile separator ','
set palette model RGB defined ( 0 'red', 1 'blue' )
unset colorbox
plot ('linear_data_train.csv')  using 2:3:1 with points palette pointtype 5
pause -1  "Hit return to continue"