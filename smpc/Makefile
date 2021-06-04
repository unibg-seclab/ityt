################################################################################
##                                                                            ##
##                            I TOLD YOU TOMORROW                             ##
##                                                                            ##
################################################################################


############################## CUSTOM VARIABLES ################################

N       ?= 3
K	:= 2
KEY	:= 88

BINS 	:= single-phase.jar two-phase-s1.jar two-phase-s2.jar
PORT    := 8080

# ... edit ARGS variables to simulate different experiments

############################### SPDZ VARIABLES #################################

# spdz configuration vars (can be set to 256 bits)

MODBIT  := 128
MAXBIT  := 128

############################## GENERAL VARIABLES ###############################

SHELL   := /bin/bash
SRCS    := $(shell find . -iname '*.java')

SERVERS ?= $(shell seq 1 $(N))
SERBINS  = $(foreach i,$(SERVERS),$(foreach bin,$(BINS),servers/server$(i)/$(bin)))
NETWORK  = $(foreach i,$(SERVERS),-p $(i):localhost:$(shell expr $(PORT) + $(i)))
NETARGS  = -e SEQUENTIAL_BATCHED -l INFO $(NETWORK)
TARGBINS = $(foreach bin,$(BINS),target/$(bin))

################################# COMPILATION ##################################

.PHONY: all test clean log watch status kill \
		runSP \
		runTPs1 \
		runTPs2 
all: $(TARGBINS)

test:
	mvn clean test

$(TARGBINS): pom.xml $(SRCS)
	mvn install -DskipTests

################################### RUN UTILS ##################################

define runserver
	@ mkdir -p servers/server$(1)
	@ cp target/$(BIN) servers/server$(1)
	cd servers/server$(1) && java -jar $(BIN) $(2) > log.txt 2>&1 &
endef

define runservers
	$(foreach i,$(SERVERS),
		$(call runserver,$(i),-i $(i) $(ARGS$(i)) $(ARGS) $(1) $(NETARGS)))
endef

run: $(TARGBINS)
	$(call runservers,-s spdz -Dspdz.preprocessingStrategy=MASCOT \
		-Dspdz.modBitLength=$(MODBIT) -Dspdz.maxBitLength=$(MAXBIT))
	@ $(MAKE) watch

############################### RUN SINGLE-PHASE ##############################

runSP: BIN = single-phase.jar 
runSP: ARGS   := -n $(N) -k $(K)
runSP: ARGS1  := -seed $(KEY)
runSP: ARGS2  := -seed  1 -salt 11
runSP: ARGS3  := -seed  2 -salt 12
runSP: ARGS4  := -seed  3 -salt 13
runSP: ARGS5  := -seed  4 -salt 14
runSP: ARGS6  := -seed  5 -salt 15
runSP: ARGS7  := -seed  6 -salt 16
runSP: ARGS8  := -seed  7 -salt 17
runSP: ARGS9  := -seed  8 -salt 18
runSP: ARGS10 := -seed  9 -salt 19
runSP: ARGS11 := -seed 10 -salt 20

runSP: run

############################# RUN TWO-PHASE Step 1 ############################

runTPs1: BIN = two-phase-s1.jar
runTPs1: ARGS   := -n $(N) -k $(K)
runTPs1: ARGS1  := -seed $(KEY)
runTPs1: ARGS2  := -seed  1
runTPs1: ARGS3  := -seed  2
runTPs1: ARGS4  := -seed  3
runTPs1: ARGS5  := -seed  4
runTPs1: ARGS6  := -seed  5
runTPs1: ARGS7  := -seed  6
runTPs1: ARGS8  := -seed  7
runTPs1: ARGS9  := -seed  8
runTPs1: ARGS10 := -seed  9
runTPs1: ARGS11 := -seed 10

runTPs1: run

############################# RUN TWO-PHASE Step 2 ############################

runTPs2: N = 2
runTPs2: BIN = two-phase-s2.jar
runTPs2: ARGS  = -coeffs $(KEY) 1
runTPs2: ARGS1 = -degree $(K)
runTPs2: ARGS2 = -degree $(K) -xval 293887421926301127572115718607716532654 -yval 293887421926301127572115718607716532742 -salt 11

runTPs2: run

##################################### EXPORT ###################################

export

##################################### CLEAN ####################################

clean:
	@ mvn clean
	@ rm -rf server* servers target

###################################### LOG #####################################

log:
	@ for i in $(SERVERS); do \
		echo -e "\n********** SERVER $$i **********"; \
		cat servers/server$$i/log.txt; \
	done;
	@ echo ''

watch:
	@- watch -n 1 $(MAKE) log --no-print-directory

#################################### STATUS ####################################

status:
	@ fuser -v $(SERBINS) || echo 'not running'

kill:
	@ fuser -vk $(SERBINS) || echo 'not running'

################################################################################

datelogger:
	touch timestart
	@echo $(shell date '+%T.%N') >> timestart

ndlogger:
	$(foreach v, $(.VARIABLES),  $(info $(v) = $($(v))))
