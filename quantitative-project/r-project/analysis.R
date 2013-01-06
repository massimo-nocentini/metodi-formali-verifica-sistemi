analysis <- function(){
  frame <- read.table("ctmc-one-element-zero-element-path.txt", header=T, quote="\"")
  cumulative.time <- cumsum(frame$time_in_state)
  frame <- data.frame(frame, cumulative.time)
  
  pdf("hist-first-model-one-element-zero-element.pdf")  
  hist(frame$cumulative.time, breaks=30, xlab="Time",main="One element/zero element in queue")
  dev.off()  
  
  
  saturationframe <- read.table("ctmc-queue-saturation-path.txt", header=T, quote="\"")
  cumulative.time <- cumsum(saturationframe$time_in_state)
  saturationframe <- data.frame(saturationframe, cumulative.time)
  
  pdf("saturation-queue.pdf")  
  # get the range for the x and y axis
  xrange <- range(saturationframe$cumulative.time)
  yrange <- range(saturationframe$q)
  
  # set up the plot
  plot(xrange, yrange, type="n", xlab="Time",
       ylab="Elements in queue", main="Saturation queue" ) 
  lines(saturationframe$cumulative.time, 
        saturationframe$q, 
        col="red",
        type="b", 
        lwd=1.5,
        lty=2, 
        pch=20) 
  dev.off()  
  
  randomframe <- read.table("ctmc-random-simulation-path.txt", header=T, quote="\"")
  cumulative.time <- cumsum(randomframe$time_in_state)
  randomframe <- data.frame(randomframe, cumulative.time)
  pdf("random-simulation-queue.pdf")  
  
  # get the range for the x and y axis
  xrange <- range(randomframe$cumulative.time)
  yrange <- range(randomframe$q)
  
  # set up the plot
  plot(xrange, yrange, type="n", xlab="Time",
       ylab="Elements in queue", main="Random simulation" ) 
  lines(randomframe$cumulative.time, 
        randomframe$q, 
        col="blue",
        type="b", 
        lwd=1.5,
        lty=2, 
        pch=20) 
  dev.off()  
  
  frame
}