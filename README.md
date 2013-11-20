test_harness
============

This is a harness for distributed testing of ccnx applications

The code is intended to read a toplogy in terms of IP numbers or host names,
connect to those machines using ssh and start certain commands on them.

The code is set up to run probabilistic experiments of the form "put documents on given server", "request documents using given probability distribution fro requests".

In the case of ALIEN this will be running ccnx on those machines, storing those documents and then requesting them from various positions on the network.
